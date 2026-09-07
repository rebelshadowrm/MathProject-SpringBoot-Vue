package com.group.mathproject.learning;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningSeedService {
    private final JdbcTemplate jdbc;

    private record SkillSeed(String code, String title, String intro, String example, String hint, String explanation, String generator, int difficulty) {}
    private record UnitSeed(String title, String description, List<SkillSeed> skills) {}
    private record CourseSeed(int grade, String slug, String title, String description, List<UnitSeed> units) {}

    @Transactional
    public void seed() {
        if (jdbc.queryForObject("select count(*) from learning_course", Integer.class) == 0) seedCourses();
        if (jdbc.queryForObject("select count(*) from classroom", Integer.class) == 0) seedClassroom();
    }

    private void seedCourses() {
        int order=0;
        for(CourseSeed course:catalog()) {
            jdbc.update("insert into learning_course(grade_level,slug,title,description,sort_order) values (?,?,?,?,?)",course.grade,course.slug,course.title,course.description,order++);
            int courseId=jdbc.queryForObject("select id from learning_course where slug=?",Integer.class,course.slug);
            int unitOrder=0;
            for(UnitSeed unit:course.units) {
                jdbc.update("insert into learning_unit(course_id,title,description,sort_order) values (?,?,?,?)",courseId,unit.title,unit.description,unitOrder++);
                int unitId=jdbc.queryForObject("select max(id) from learning_unit where course_id=?",Integer.class,courseId);
                int skillOrder=0;
                for(SkillSeed skill:unit.skills) jdbc.update("insert into learning_skill(unit_id,code,title,introduction,worked_example,hint_text,explanation,generator_type,difficulty,sort_order) values (?,?,?,?,?,?,?,?,?,?)",
                        unitId,skill.code,skill.title,skill.intro,skill.example,skill.hint,skill.explanation,skill.generator,skill.difficulty,skillOrder++);
            }
        }
    }

    private void seedClassroom() {
        Integer teacher=id("Teacher"),student=id("Student"),parent=id("Parent");
        if(teacher==null||student==null||parent==null)return;
        jdbc.update("insert into classroom(name,grade_level,teacher_id) values (?,?,?)","Ms. Rivera's Grade 4",4,teacher);
        int classroom=jdbc.queryForObject("select max(id) from classroom where teacher_id=?",Integer.class,teacher);
        jdbc.update("insert into classroom_enrollment(classroom_id,student_id) values (?,?)",classroom,student);
        jdbc.update("insert into parent_student(parent_id,student_id) values (?,?)",parent,student);
        jdbc.update("insert into announcement(classroom_id,author_id,title,body) values (?,?,?,?)",classroom,teacher,"Welcome to Math Practice","Start with the place value check-in, then choose a skill to practice.");
        jdbc.update("insert into assessment(creator_id,title,description,published) values (?,?,?,true)",teacher,"Grade 4 Place Value Check-in","A short baseline assessment covering arithmetic and place value.");
        int assessment=jdbc.queryForObject("select max(id) from assessment where creator_id=?",Integer.class,teacher);
        int addition=skillId("g4-multi-digit-addition"), multiplication=skillId("g4-multiplication");
        addAssessmentQuestion(assessment,addition,"2,405 + 1,320 = ?",List.of("3,625","3,725","3,825","4,725"),"3,725",0,"Add each place value from right to left.");
        addAssessmentQuestion(assessment,multiplication,"24 × 6 = ?",List.of("124","134","144","154"),"144",1,"Break 24 into 20 and 4, then multiply each by 6.");
        addAssessmentQuestion(assessment,addition,"Round 4,682 to the nearest hundred.",List.of("4,600","4,680","4,700","5,000"),"4,700",2,"The tens digit is 8, so round the hundreds place up.");
        jdbc.update("insert into assignment(classroom_id,assessment_id,due_at) values (?,?,?)",classroom,assessment,LocalDateTime.now().plusDays(7));
    }

    private void addAssessmentQuestion(int assessment,int skill,String prompt,List<String> answers,String correct,int order,String explanation){jdbc.update("insert into assessment_question(assessment_id,skill_id,prompt,answer_a,answer_b,answer_c,answer_d,correct_answer,hint_text,explanation,sort_order) values (?,?,?,?,?,?,?,?,?,?,?)",assessment,skill,prompt,answers.get(0),answers.get(1),answers.get(2),answers.get(3),correct,"Think about what the question is asking.",explanation,order);}
    private Integer id(String username){List<Integer> ids=jdbc.queryForList("select id from app_user where username=?",Integer.class,username);return ids.isEmpty()?null:ids.getFirst();}
    private int skillId(String code){return jdbc.queryForObject("select id from learning_skill where code=?",Integer.class,code);}
    private SkillSeed s(String code,String title,String intro,String example,String generator,int difficulty){return new SkillSeed(code,title,intro,example,"Break the problem into a smaller fact you already know.","Review the place values and inverse operations, then check the result.",generator,difficulty);}
    private UnitSeed u(String title,String description,SkillSeed...skills){return new UnitSeed(title,description,List.of(skills));}

    private List<CourseSeed> catalog(){return List.of(
        new CourseSeed(3,"grade-3","Grade 3 Math","Build multiplication, division, fraction, and measurement foundations.",List.of(
            u("Multiplication & division","Use equal groups and related facts.",s("g3-multiplication","Multiply within 100","Multiplication combines equal groups.","4 groups of 6 makes 4 × 6 = 24.","MULTIPLICATION",1),s("g3-division","Divide within 100","Division separates a total into equal groups.","24 ÷ 6 = 4 because 4 × 6 = 24.","DIVISION",1)),
            u("Place value & arithmetic","Calculate and round within 1,000.",s("g3-add-sub","Add and subtract within 1,000","Line up numbers by place value.","326 + 140 = 466.","ADDITION",1),s("g3-rounding","Round whole numbers","Look at the digit to the right of the target place.","267 rounds to 300 to the nearest hundred.","ADDITION",1)),
            u("Fractions","Understand fractions as numbers.",s("g3-fractions","Unit fractions","A denominator names equal parts of one whole.","Three of eight equal parts is 3/8.","FRACTION",1)),
            u("Measurement & shapes","Connect multiplication to area and perimeter.",s("g3-area-perimeter","Area and perimeter","Area covers a shape; perimeter travels around it.","A 4 by 3 rectangle has area 12 and perimeter 14.","GEOMETRY",1))
        )),
        new CourseSeed(4,"grade-4","Grade 4 Math","Extend whole-number operations and fraction reasoning.",List.of(
            u("Multi-digit operations","Use place value for efficient calculation.",s("g4-multi-digit-addition","Multi-digit addition","Regroup when a place totals ten or more.","2,405 + 1,320 = 3,725.","ADDITION",2),s("g4-multiplication","Multi-digit multiplication","Use place value and partial products.","24 × 6 = 120 + 24 = 144.","MULTIPLICATION",2)),
            u("Factors & patterns","Find factor pairs and number patterns.",s("g4-factors","Factors and multiples","Factors multiply to make a number.","3 and 8 are factors of 24.","MULTIPLICATION",1)),
            u("Fractions & decimals","Compare equivalent values.",s("g4-fraction-equivalence","Equivalent fractions","Multiply numerator and denominator by the same number.","1/2 = 2/4.","FRACTION",2),s("g4-decimals","Tenths and hundredths","Decimals use place value to show parts of one.","0.7 means seven tenths.","DECIMAL",1)),
            u("Angles & geometry","Measure angles and classify shapes.",s("g4-geometry","Angles and shapes","Angles describe turns and corners.","A right angle measures 90 degrees.","GEOMETRY",2))
        )),
        new CourseSeed(5,"grade-5","Grade 5 Math","Develop fluency with fractions, decimals, volume, and coordinates.",List.of(
            u("Expressions & decimals","Read expressions and calculate with decimals.",s("g5-expressions","Numerical expressions","Use grouping and order of operations.","3 + 2 × 5 = 13.","EXPRESSION",2),s("g5-decimal-operations","Decimal operations","Align decimal points by place value.","1.4 + 0.8 = 2.2.","DECIMAL",2)),
            u("Fraction operations","Add, subtract, and multiply fractions.",s("g5-fraction-operations","Fraction operations","Use common denominators before adding.","1/4 + 2/4 = 3/4.","FRACTION",3)),
            u("Volume & data","Measure three-dimensional space and interpret data.",s("g5-volume","Volume","Multiply length × width × height.","A 2 × 3 × 4 prism has volume 24.","GEOMETRY",2)),
            u("Coordinate geometry","Locate points on a coordinate plane.",s("g5-coordinate-plane","Coordinate plane","Move across for x, then up for y.","(3, 2) is three right and two up.","GEOMETRY",2))
        )),
        new CourseSeed(6,"grade-6","Grade 6 Math","Connect ratios, rational numbers, algebra, geometry, and statistics.",List.of(
            u("Ratios & rates","Reason about equivalent relationships.",s("g6-ratios","Ratios and rates","A ratio compares two quantities.","At 3 items for $6, each item costs $2.","RATIO",2)),
            u("Rational numbers","Operate with fractions and signed values.",s("g6-rational-numbers","Rational numbers","Positive and negative numbers lie on opposite sides of zero.","The opposite of −4 is 4.","DIVISION",3)),
            u("Expressions & equations","Use variables to represent unknown values.",s("g6-equations","Expressions and equations","Keep an equation balanced by doing the same thing to both sides.","x + 5 = 12, so x = 7.","EXPRESSION",3)),
            u("Geometry & statistics","Find measures and summarize distributions.",s("g6-geometry","Area and volume","Decompose figures into familiar shapes.","Add the areas of non-overlapping rectangles.","GEOMETRY",3),s("g6-statistics","Statistical center","The mean shares a total equally.","The mean of 4 and 8 is 6.","STATISTICS",2))
        ))
    );}
}
