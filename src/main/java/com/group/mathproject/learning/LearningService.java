package com.group.mathproject.learning;

import com.group.mathproject.learning.LearningDtos.*;
import com.group.mathproject.model.User;
import com.group.mathproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningService {
    private final JdbcTemplate jdbc;
    private final UserService userService;

    public List<Course> courses(String username) {
        Integer studentId = optionalUserId(username);
        return jdbc.query("select id, grade_level, slug, title, description from learning_course order by sort_order", (rs, n) ->
                new Course(rs.getInt("id"), rs.getInt("grade_level"), rs.getString("slug"),
                        rs.getString("title"), rs.getString("description"), units(rs.getInt("id"), studentId)));
    }

    private List<Unit> units(Integer courseId, Integer studentId) {
        return jdbc.query("select id, title, description from learning_unit where course_id=? order by sort_order", (rs, n) ->
                new Unit(rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                        skills(rs.getInt("id"), studentId)), courseId);
    }

    private List<Skill> skills(Integer unitId, Integer studentId) {
        String sql = "select s.id,s.code,s.title,s.introduction,s.worked_example,s.hint_text,s.explanation," +
                "coalesce(m.status,'NOT_STARTED') status,coalesce(m.xp,0) xp from learning_skill s " +
                "left join skill_mastery m on m.skill_id=s.id and m.student_id=? where s.unit_id=? order by s.sort_order";
        return jdbc.query(sql, (rs, n) -> new Skill(rs.getInt("id"), rs.getString("code"), rs.getString("title"),
                rs.getString("introduction"), rs.getString("worked_example"), rs.getString("hint_text"),
                rs.getString("explanation"), rs.getString("status"), rs.getInt("xp")),
                studentId == null ? -1 : studentId, unitId);
    }

    public PracticeSession startSession(String username, SessionRequest request) {
        Integer userId = requiredUserId(username);
        String mode = Optional.ofNullable(request.mode()).orElse("DRILL").toUpperCase(Locale.ROOT);
        if (!Set.of("DRILL", "FLASHCARD").contains(mode)) throw new IllegalArgumentException("Mode must be DRILL or FLASHCARD");
        int count = Math.max(1, Math.min(Optional.ofNullable(request.questionCount()).orElse(5), 20));
        List<String> codes = Optional.ofNullable(request.skillCodes()).orElse(List.of()).stream().filter(Objects::nonNull).distinct().toList();
        if (codes.isEmpty()) throw new IllegalArgumentException("Choose at least one skill");
        List<Map<String, Object>> skillRows = new ArrayList<>();
        for (String code : codes) {
            skillRows.addAll(jdbc.queryForList("select id,code,title,generator_type,difficulty,hint_text,explanation from learning_skill where code=?", code));
        }
        if (skillRows.isEmpty()) throw new IllegalArgumentException("No valid skills were selected");
        Integer sessionId = insertAndReturnId("insert into practice_session(student_id,mode,status) values (?,?,?)", userId, mode, "ACTIVE");
        List<PracticeQuestion> questions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> skill = skillRows.get(i % skillRows.size());
            Generated generated = generate((String) skill.get("generator_type"), ((Number) skill.get("difficulty")).intValue());
            Integer questionId = insertAndReturnId("insert into practice_question(session_id,skill_id,prompt,answer_a,answer_b,answer_c,answer_d,correct_answer,sort_order) values (?,?,?,?,?,?,?,?,?)",
                    sessionId, skill.get("id"), generated.prompt, generated.answers.get(0), generated.answers.get(1),
                    generated.answers.get(2), generated.answers.get(3), generated.correct, i);
            questions.add(new PracticeQuestion(questionId, (String) skill.get("code"), (String) skill.get("title"), generated.prompt,
                    generated.answers, mode.equals("FLASHCARD") ? generated.correct : null,
                    (String) skill.get("hint_text"), (String) skill.get("explanation")));
        }
        return new PracticeSession(sessionId, mode, "ACTIVE", questions);
    }

    public SessionResult completeSession(String username, Integer sessionId, SessionSubmission submission) {
        Integer userId = requiredUserId(username);
        Integer owner = jdbc.queryForObject("select student_id from practice_session where id=?", Integer.class, sessionId);
        if (!Objects.equals(owner, userId)) throw new AccessDeniedException("This session belongs to another learner");
        Map<Integer, String> selected = new HashMap<>();
        Optional.ofNullable(submission.answers()).orElse(List.of()).forEach(a -> selected.put(a.questionId(), a.answer()));
        List<Map<String, Object>> rows = jdbc.queryForList("select pq.id,pq.skill_id,pq.prompt,pq.correct_answer,s.explanation from practice_question pq join learning_skill s on s.id=pq.skill_id where pq.session_id=? order by pq.sort_order", sessionId);
        int score = 0;
        List<Feedback> feedback = new ArrayList<>();
        Set<Integer> sessionSkills = new HashSet<>();
        for (Map<String, Object> row : rows) {
            int questionId = ((Number) row.get("id")).intValue();
            int skillId = ((Number) row.get("skill_id")).intValue();
            String answer = selected.getOrDefault(questionId, "");
            String correctAnswer = (String) row.get("correct_answer");
            boolean correct = correctAnswer.equalsIgnoreCase(answer.trim());
            if (correct) score++;
            sessionSkills.add(skillId);
            jdbc.update("update practice_question set selected_answer=?,answered_correct=? where id=?", answer, correct, questionId);
            updateMastery(userId, skillId, correct);
            feedback.add(new Feedback(questionId, (String) row.get("prompt"), answer, correctAnswer, correct, (String) row.get("explanation")));
        }
        sessionSkills.forEach(skillId -> jdbc.update("update skill_mastery set sessions_completed=sessions_completed+1 where student_id=? and skill_id=?", userId, skillId));
        sessionSkills.forEach(skillId -> refreshMastery(userId, skillId));
        jdbc.update("update practice_session set status='COMPLETED',completed_at=? where id=?", LocalDateTime.now(), sessionId);
        return new SessionResult(score, rows.size(), score * 10, feedback);
    }

    private void updateMastery(int studentId, int skillId, boolean correct) {
        Integer count = jdbc.queryForObject("select count(*) from skill_mastery where student_id=? and skill_id=?", Integer.class, studentId, skillId);
        if (count == 0) jdbc.update("insert into skill_mastery(student_id,skill_id,attempts,correct_answers,status,xp) values (?,?,?,?,?,?)",
                studentId, skillId, 1, correct ? 1 : 0, "DEVELOPING", correct ? 10 : 0);
        else jdbc.update("update skill_mastery set attempts=attempts+1,correct_answers=correct_answers+?,xp=xp+?,updated_at=? where student_id=? and skill_id=?",
                correct ? 1 : 0, correct ? 10 : 0, LocalDateTime.now(), studentId, skillId);
    }

    private void refreshMastery(int studentId, int skillId) {
        Map<String, Object> row = jdbc.queryForMap("select attempts,correct_answers,sessions_completed from skill_mastery where student_id=? and skill_id=?", studentId, skillId);
        int attempts = ((Number) row.get("attempts")).intValue();
        int correct = ((Number) row.get("correct_answers")).intValue();
        int sessions = ((Number) row.get("sessions_completed")).intValue();
        double accuracy = attempts == 0 ? 0 : (double) correct / attempts;
        String status = attempts < 5 ? "DEVELOPING" : accuracy >= .9 && sessions >= 2 ? "MASTERED" : accuracy >= .8 ? "PROFICIENT" : "DEVELOPING";
        jdbc.update("update skill_mastery set status=?,updated_at=? where student_id=? and skill_id=?", status, LocalDateTime.now(), studentId, skillId);
    }

    public List<Announcement> announcements(String username) {
        Integer id = requiredUserId(username);
        Integer admin = jdbc.queryForObject("select count(*) from app_user_roles ur join role r on r.id=ur.roles_id where ur.user_id=? and r.name='ROLE_ADMIN'", Integer.class, id);
        if (admin > 0) return jdbc.query("select a.id,a.title,a.body,c.name class_name,u.username author,a.published_at from announcement a join classroom c on c.id=a.classroom_id join app_user u on u.id=a.author_id order by a.published_at desc",
                (rs,n) -> new Announcement(rs.getInt("id"),rs.getString("title"),rs.getString("body"),rs.getString("class_name"),rs.getString("author"),rs.getTimestamp("published_at").toLocalDateTime()));
        String sql = "select distinct a.id,a.title,a.body,c.name class_name,u.username author,a.published_at from announcement a " +
                "join classroom c on c.id=a.classroom_id join app_user u on u.id=a.author_id " +
                "left join classroom_enrollment e on e.classroom_id=c.id left join parent_student ps on ps.student_id=e.student_id " +
                "where c.teacher_id=? or e.student_id=? or ps.parent_id=? order by a.published_at desc";
        return jdbc.query(sql, (rs,n) -> new Announcement(rs.getInt("id"), rs.getString("title"), rs.getString("body"),
                rs.getString("class_name"), rs.getString("author"), rs.getTimestamp("published_at").toLocalDateTime()), id,id,id);
    }

    public List<Assignment> assignments(String username) {
        Integer id = requiredUserId(username);
        String sql = "select a.id,x.title,x.description,c.name class_name,a.due_at,s.score,s.total from assignment a " +
                "join assessment x on x.id=a.assessment_id join classroom c on c.id=a.classroom_id " +
                "join classroom_enrollment e on e.classroom_id=c.id left join assignment_submission s on s.assignment_id=a.id and s.student_id=? " +
                "where e.student_id=? and x.published=true order by a.due_at";
        return jdbc.query(sql, (rs,n) -> new Assignment(rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                rs.getString("class_name"), rs.getTimestamp("due_at") == null ? null : rs.getTimestamp("due_at").toLocalDateTime(),
                rs.getObject("score") != null, (Integer) rs.getObject("score"), (Integer) rs.getObject("total"),
                assignmentQuestions(rs.getInt("id"))), id,id);
    }

    private List<AssignmentQuestion> assignmentQuestions(int assignmentId) {
        String sql = "select q.id,s.code,q.prompt,q.answer_a,q.answer_b,q.answer_c,q.answer_d from assignment a join assessment_question q on q.assessment_id=a.assessment_id left join learning_skill s on s.id=q.skill_id where a.id=? order by q.sort_order";
        return jdbc.query(sql, (rs,n) -> new AssignmentQuestion(rs.getInt("id"), rs.getString("code"), rs.getString("prompt"),
                List.of(rs.getString("answer_a"),rs.getString("answer_b"),rs.getString("answer_c"),rs.getString("answer_d"))), assignmentId);
    }

    public SessionResult submitAssignment(String username, int assignmentId, AssignmentSubmission submission) {
        int studentId = requiredUserId(username);
        Integer enrolled = jdbc.queryForObject("select count(*) from classroom_enrollment e join assignment a on a.classroom_id=e.classroom_id where a.id=? and e.student_id=?", Integer.class, assignmentId, studentId);
        if (enrolled == 0) throw new AccessDeniedException("Assignment is not assigned to this learner");
        Map<Integer,String> answers = new HashMap<>();
        Optional.ofNullable(submission.answers()).orElse(List.of()).forEach(a -> answers.put(a.questionId(), a.answer()));
        List<Map<String,Object>> rows = jdbc.queryForList("select q.id,q.skill_id,q.prompt,q.correct_answer,q.explanation from assignment a join assessment_question q on q.assessment_id=a.assessment_id where a.id=? order by q.sort_order", assignmentId);
        int score=0; Set<Integer> touchedSkills=new HashSet<>();
        List<Feedback> feedback=new ArrayList<>();
        for (Map<String,Object> row: rows) {
            int qid=((Number)row.get("id")).intValue(); String chosen=answers.getOrDefault(qid,""); String correct=(String)row.get("correct_answer"); boolean ok=correct.equalsIgnoreCase(chosen.trim());
            if(ok) score++;
            if(row.get("skill_id") != null) { int skillId=((Number)row.get("skill_id")).intValue(); updateMastery(studentId,skillId,ok); touchedSkills.add(skillId); }
            feedback.add(new Feedback(qid,(String)row.get("prompt"),chosen,correct,ok,(String)row.get("explanation")));
        }
        touchedSkills.forEach(skillId -> { jdbc.update("update skill_mastery set sessions_completed=sessions_completed+1 where student_id=? and skill_id=?",studentId,skillId); refreshMastery(studentId,skillId); });
        jdbc.update("delete from assignment_submission where assignment_id=? and student_id=?", assignmentId,studentId);
        jdbc.update("insert into assignment_submission(assignment_id,student_id,score,total,answers) values (?,?,?,?,?)", assignmentId,studentId,score,rows.size(),String.valueOf(submission.answers()));
        return new SessionResult(score,rows.size(),score*10,feedback);
    }

    public Progress progress(String viewer, String learner) {
        int learnerId=requiredUserId(learner);
        if(!viewer.equals(learner) && !canViewLearner(requiredUserId(viewer), learnerId)) throw new AccessDeniedException("Learner progress is private");
        Map<String,Object> totals=jdbc.queryForMap("select coalesce(sum(xp),0) xp,coalesce(sum(case when status='MASTERED' then 1 else 0 end),0) mastered,coalesce(sum(case when status='PROFICIENT' then 1 else 0 end),0) proficient,coalesce(sum(case when status='DEVELOPING' then 1 else 0 end),0) developing from skill_mastery where student_id=?",learnerId);
        return new Progress(learner, num(totals,"xp"),num(totals,"mastered"),num(totals,"proficient"),num(totals,"developing"),courses(learner));
    }

    public List<LeaderboardEntry> leaderboard(String username,String scope,Integer classId) {
        int userId=requiredUserId(username);
        String where=""; List<Object> args=new ArrayList<>();
        if("class".equalsIgnoreCase(scope)) {
            int resolved=visibleClassId(userId,classId);
            where="where u.id in (select student_id from classroom_enrollment where classroom_id=?)"; args.add(resolved);
        } else where="where exists (select 1 from app_user_roles ur join role r on r.id=ur.roles_id where ur.user_id=u.id and r.name='ROLE_STUDENT')";
        String sql="select u.username,coalesce(sum(m.xp),0) xp,coalesce(max(c.grade_level),0) grade from app_user u left join skill_mastery m on m.student_id=u.id left join classroom_enrollment e on e.student_id=u.id left join classroom c on c.id=e.classroom_id "+where+" group by u.id,u.username order by xp desc,u.username";
        List<Map<String,Object>> rows=jdbc.queryForList(sql,args.toArray()); List<LeaderboardEntry> result=new ArrayList<>(); int rank=1;
        for(Map<String,Object> row:rows) result.add(new LeaderboardEntry(rank++,(String)row.get("username"),num(row,"xp"),num(row,"grade")));
        return result;
    }

    public Dashboard dashboard(Authentication auth) {
        String username=auth.getName(); String role=auth.getAuthorities().stream().map(Object::toString).sorted().findFirst().orElse("ROLE_STUDENT").replace("ROLE_","");
        List<Classroom> classes=classrooms(username,auth); List<Announcement> news=announcements(username);
        List<Assignment> work=role.equals("STUDENT")?assignments(username):List.of(); List<Progress> learners=new ArrayList<>();
        if(role.equals("PARENT")||role.equals("TEACHER")||role.equals("ADMIN")) for(String child:visibleLearners(username,role)) learners.add(progress(username,child));
        return new Dashboard(role,username,classes,news,work,learners);
    }

    public List<Classroom> classrooms(String username,Authentication auth) {
        int id=requiredUserId(username); boolean admin=has(auth,"ROLE_ADMIN");
        String sql=admin?"select c.id,c.name,c.grade_level,u.username teacher from classroom c join app_user u on u.id=c.teacher_id order by c.name":
                "select distinct c.id,c.name,c.grade_level,u.username teacher from classroom c join app_user u on u.id=c.teacher_id left join classroom_enrollment e on e.classroom_id=c.id left join parent_student ps on ps.student_id=e.student_id where c.teacher_id=? or e.student_id=? or ps.parent_id=? order by c.name";
        Object[] args=admin?new Object[]{}:new Object[]{id,id,id};
        return jdbc.query(sql,(rs,n)->new Classroom(rs.getInt("id"),rs.getString("name"),rs.getInt("grade_level"),rs.getString("teacher"),
                jdbc.queryForList("select u.username from classroom_enrollment e join app_user u on u.id=e.student_id where e.classroom_id=? order by u.username",String.class,rs.getInt("id"))),args);
    }

    public Classroom createClass(String username,CreateClass request) {
        int teacher=requiredUserId(username); int grade=Math.max(3,Math.min(6,request.grade()));
        int id=insertAndReturnId("insert into classroom(name,grade_level,teacher_id) values (?,?,?)",request.name(),grade,teacher);
        return new Classroom(id,request.name(),grade,username,List.of());
    }

    public Map<String,String> createStudent(String teacher,int classId,CreateStudent request) {
        assertTeacherOwns(teacher,classId); String temporary=UUID.randomUUID().toString().substring(0,10);
        User user=new User(null,request.username(),temporary,request.email(),request.firstName(),request.lastName(),new ArrayList<>());
        userService.saveUser(user); userService.addRoleToUser(request.username(),"ROLE_STUDENT"); int id=requiredUserId(request.username());
        jdbc.update("update app_user set password_change_required=true where id=?", id);
        jdbc.update("insert into classroom_enrollment(classroom_id,student_id) values (?,?)",classId,id);
        return Map.of("username",request.username(),"temporaryPassword",temporary);
    }

    public Announcement createAnnouncement(String teacher,int classId,CreateAnnouncement request) {
        assertTeacherOwns(teacher,classId); int author=requiredUserId(teacher);
        int id=insertAndReturnId("insert into announcement(classroom_id,author_id,title,body) values (?,?,?,?)",classId,author,request.title(),request.body());
        String className=jdbc.queryForObject("select name from classroom where id=?",String.class,classId);
        return new Announcement(id,request.title(),request.body(),className,teacher,LocalDateTime.now());
    }

    public Assignment createAssignment(String teacher,int classId,CreateAssignment request) {
        assertTeacherOwns(teacher,classId); int creator=requiredUserId(teacher);
        List<String> codes=Optional.ofNullable(request.skillCodes()).orElse(List.of()).stream().distinct().toList();
        if(codes.isEmpty())throw new IllegalArgumentException("Choose at least one skill");
        int assessment=insertAndReturnId("insert into assessment(creator_id,title,description,published) values (?,?,?,true)",creator,request.title(),request.description());
        int count=Math.max(1,Math.min(Optional.ofNullable(request.questionCount()).orElse(5),20));
        for(int i=0;i<count;i++){
            String code=codes.get(i%codes.size());Map<String,Object> skill=jdbc.queryForMap("select id,generator_type,difficulty,hint_text,explanation from learning_skill where code=?",code);
            Generated q=generate((String)skill.get("generator_type"),((Number)skill.get("difficulty")).intValue());
            jdbc.update("insert into assessment_question(assessment_id,skill_id,prompt,answer_a,answer_b,answer_c,answer_d,correct_answer,hint_text,explanation,sort_order) values (?,?,?,?,?,?,?,?,?,?,?)",assessment,skill.get("id"),q.prompt,q.answers.get(0),q.answers.get(1),q.answers.get(2),q.answers.get(3),q.correct,skill.get("hint_text"),skill.get("explanation"),i);
        }
        int assignmentId=insertAndReturnId("insert into assignment(classroom_id,assessment_id,due_at) values (?,?,?)",classId,assessment,request.dueAt());
        String className=jdbc.queryForObject("select name from classroom where id=?",String.class,classId);
        return new Assignment(assignmentId,request.title(),request.description(),className,request.dueAt(),false,null,null,assignmentQuestions(assignmentId));
    }

    public void linkParent(String actor,String studentUsername,String parentUsername) {
        int actorId=requiredUserId(actor),student=requiredUserId(studentUsername),parent=requiredUserId(parentUsername);
        if(!canViewLearner(actorId,student))throw new AccessDeniedException("You cannot manage this learner");
        Integer exists=jdbc.queryForObject("select count(*) from parent_student where parent_id=? and student_id=?",Integer.class,parent,student);
        if(exists==0)jdbc.update("insert into parent_student(parent_id,student_id) values (?,?)",parent,student);
    }

    private void assertTeacherOwns(String username,int classId) { Integer count=jdbc.queryForObject("select count(*) from classroom where id=? and teacher_id=?",Integer.class,classId,requiredUserId(username)); if(count==0) throw new AccessDeniedException("Class belongs to another teacher"); }
    private boolean canViewLearner(int viewer,int learner){ Integer n=jdbc.queryForObject("select count(*) from parent_student where parent_id=? and student_id=?",Integer.class,viewer,learner); if(n>0)return true; n=jdbc.queryForObject("select count(*) from classroom c join classroom_enrollment e on e.classroom_id=c.id where c.teacher_id=? and e.student_id=?",Integer.class,viewer,learner); if(n>0)return true; return jdbc.queryForObject("select count(*) from app_user_roles ur join role r on r.id=ur.roles_id where ur.user_id=? and r.name='ROLE_ADMIN'",Integer.class,viewer)>0; }
    private List<String> visibleLearners(String username,String role){int id=requiredUserId(username);if(role.equals("PARENT"))return jdbc.queryForList("select u.username from parent_student p join app_user u on u.id=p.student_id where p.parent_id=?",String.class,id);if(role.equals("TEACHER"))return jdbc.queryForList("select distinct u.username from classroom c join classroom_enrollment e on e.classroom_id=c.id join app_user u on u.id=e.student_id where c.teacher_id=?",String.class,id);return jdbc.queryForList("select distinct u.username from app_user u join app_user_roles ur on ur.user_id=u.id join role r on r.id=ur.roles_id where r.name='ROLE_STUDENT'",String.class);}
    private boolean has(Authentication auth,String role){return auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals(role));}
    private int visibleClassId(int userId,Integer requested){
        String visible="select c.id from classroom c left join classroom_enrollment e on e.classroom_id=c.id left join parent_student ps on ps.student_id=e.student_id where c.teacher_id=? or e.student_id=? or ps.parent_id=? or exists (select 1 from app_user_roles ur join role r on r.id=ur.roles_id where ur.user_id=? and r.name='ROLE_ADMIN')";
        List<Integer> ids=jdbc.queryForList(visible,Integer.class,userId,userId,userId,userId);
        if(requested!=null){if(!ids.contains(requested))throw new AccessDeniedException("Class leaderboard is private");return requested;}
        if(ids.isEmpty())throw new IllegalArgumentException("Join a class to view classroom rankings");return ids.getFirst();
    }
    private Integer optionalUserId(String username){if(username==null)return null;List<Integer> ids=jdbc.queryForList("select id from app_user where username=?",Integer.class,username);return ids.isEmpty()?null:ids.getFirst();}
    private Integer requiredUserId(String username){Integer id=optionalUserId(username);if(id==null)throw new IllegalArgumentException("Unknown user");return id;}
    private int num(Map<String,Object> map,String key){return ((Number)map.get(key)).intValue();}
    private Integer insertAndReturnId(String sql,Object...args){org.springframework.jdbc.support.GeneratedKeyHolder kh=new org.springframework.jdbc.support.GeneratedKeyHolder();jdbc.update(c->{var ps=c.prepareStatement(sql,new String[]{"id"});for(int i=0;i<args.length;i++)ps.setObject(i+1,args[i]);return ps;},kh);return Objects.requireNonNull(kh.getKey()).intValue();}

    private Generated generate(String type,int difficulty){
        int scale=Math.max(1,Math.min(3,difficulty)); ThreadLocalRandom r=ThreadLocalRandom.current(); int a=r.nextInt(2,10*(scale+1)),b=r.nextInt(2,10*(scale+1)); int value;String prompt;
        switch(type){
            case "SUBTRACTION"->{if(b>a){int t=a;a=b;b=t;}value=a-b;prompt=a+" − "+b+" = ?";}
            case "MULTIPLICATION"->{a=r.nextInt(2,13);b=r.nextInt(2,13);value=a*b;prompt=a+" × "+b+" = ?";}
            case "DIVISION"->{b=r.nextInt(2,13);value=r.nextInt(2,13);a=b*value;prompt=a+" ÷ "+b+" = ?";}
            case "FRACTION"->{b=r.nextInt(2,10);value=b;prompt="One whole is split into "+b+" equal parts. What denominator names one part?";}
            case "DECIMAL"->{value=a+b;prompt=String.format(Locale.ROOT,"%.1f + %.1f = ?",a/10.0,b/10.0);return choices(prompt,String.format(Locale.ROOT,"%.1f",value/10.0),r);}
            case "RATIO"->{int k=r.nextInt(2,6);value=a*k;prompt="If the ratio is 1:"+k+", what pairs with "+a+"?";}
            case "EXPRESSION"->{value=a+2*b;prompt=a+" + 2 × "+b+" = ?";}
            case "GEOMETRY"->{value=2*(a+b);prompt="A rectangle is "+a+" by "+b+". What is its perimeter?";}
            case "STATISTICS"->{value=(a+b);prompt="Two equal groups total "+(2*value)+". What is their mean?";}
            default->{value=a+b;prompt=a+" + "+b+" = ?";}
        }
        return choices(prompt,String.valueOf(value),r);
    }
    private Generated choices(String prompt,String correct,ThreadLocalRandom r){double base=Double.parseDouble(correct);Set<String> values=new LinkedHashSet<>();values.add(correct);while(values.size()<4){double d=base+r.nextInt(-5,6);String v=correct.contains(".")?String.format(Locale.ROOT,"%.1f",d):String.valueOf((int)d);values.add(v);}List<String> list=new ArrayList<>(values);Collections.shuffle(list,r);return new Generated(prompt,list,correct);}
    private record Generated(String prompt,List<String> answers,String correct){}
}
