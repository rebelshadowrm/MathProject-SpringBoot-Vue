// @vitest-environment jsdom
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import Home from './Home.vue'

describe('Math Practice home', () => {
  it('presents the learning purpose and a clear demo action', () => {
    const wrapper = mount(Home, { global: { stubs: { RouterLink: { template: '<a><slot /></a>' } } } })
    expect(wrapper.text()).toContain('Small steps build strong math.')
    expect(wrapper.text()).toContain('Explore the demo')
    expect(wrapper.text()).not.toContain("It's bananas")
  })
})
