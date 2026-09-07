<template>
  <section class="hero page-shell">
    <div class="hero-copy">
      <p class="eyebrow">Grades 3–6 · Learn at your pace</p>
      <h1>Small steps build <span>strong math.</span></h1>
      <p class="lede">Practice a skill, understand the result, and see your progress grow. Math Practice connects learners, teachers, and families around one clear learning path.</p>
      <div class="actions"><router-link class="button primary" to="/login">Explore the demo</router-link><a class="button secondary" href="#how">How it works</a></div>
    </div>
    <div class="hero-card glass-card" aria-label="Example learning progress">
      <p class="eyebrow">Today’s path</p><h2>Grade 4 Math</h2>
      <div class="progress"><span style="width:68%"></span></div><p>68% of current skills in progress</p>
      <ul><li><span>✓</span> Multi-digit addition</li><li><span>→</span> Fraction equivalence</li><li><span>○</span> Angles and shapes</li></ul>
    </div>
  </section>
  <section id="how" class="page-shell section-block">
    <p class="eyebrow">A complete learning loop</p><h2>Practice with purpose</h2>
    <div class="feature-grid"><article class="glass-card"><b>01</b><h3>Choose a path</h3><p>Follow focused courses from foundational arithmetic through early algebra.</p></article><article class="glass-card"><b>02</b><h3>Learn by doing</h3><p>Use examples and hints, then build confidence with drills and flashcards.</p></article><article class="glass-card"><b>03</b><h3>See the growth</h3><p>Mastery, assignments, and classroom insight keep everyone moving together.</p></article></div>
  </section>
</template>
<script>
export default { name: 'Home', created(){ document.title='Math Practice' } }
</script>
<style scoped>
.hero{min-height:calc(100vh - 4.5rem);display:grid;grid-template-columns:1.25fr .75fr;align-items:center;gap:4rem;padding-block:6rem}.hero h1{font:800 clamp(3.4rem,8vw,7rem)/.95 var(--ff-serif);max-width:10ch}.hero h1 span{color:hsl(var(--clr-accent-200))}.lede{font-size:1.2rem;margin:1.5rem 0 2rem}.actions{display:flex;gap:1rem;flex-wrap:wrap}.hero-card{padding:2rem;transform:rotate(1.5deg)}.hero-card h2{font-size:2rem}.hero-card ul{list-style:none;padding:0;margin:2rem 0 0;display:grid;gap:1rem}.hero-card li{display:flex;gap:.75rem}.hero-card li span{color:hsl(var(--clr-accent-200));font-weight:800}.progress{height:.65rem;background:hsl(var(--clr-white-200)/.1);border-radius:1rem;margin:1.25rem 0 .5rem;overflow:hidden}.progress span{height:100%;display:block;background:hsl(var(--clr-accent-400))}.section-block{padding-block:5rem}.section-block>h2{font:700 clamp(2.5rem,5vw,4rem) var(--ff-serif);margin-bottom:2rem}.feature-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:1.25rem}.feature-grid article{padding:1.75rem}.feature-grid b{color:hsl(var(--clr-secondary-200));font-family:var(--ff-mono)}.feature-grid h3{font-size:1.35rem;margin:.8rem 0}.feature-grid p{color:hsl(var(--clr-white-600))}@media(max-width:48rem){.hero{grid-template-columns:1fr;padding-block:4rem;gap:2rem}.feature-grid{grid-template-columns:1fr}}
</style>
