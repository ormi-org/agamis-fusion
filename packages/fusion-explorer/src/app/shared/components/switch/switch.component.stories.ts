import { Meta, moduleMetadata } from '@storybook/angular'
import { SharedModule } from '@shared/shared.module'
import { customViewport } from './.storybook'
import { MINIMAL_VIEWPORTS } from '@storybook/addon-viewport'
import { SwitchComponent } from './switch.component'

export default {
  title: 'Shared/Switch',
  component: SwitchComponent,
  parameters: {
    viewport: {
      viewports: {
        ...customViewport,
        ...MINIMAL_VIEWPORTS
      },
      defaultViewport: 'shared-separator-default'
    }
  },
  decorators: [
    moduleMetadata({
      imports: [SharedModule]
    })
  ]
} as Meta<SwitchComponent>

export const Default = {
  render: (args: SwitchComponent) => ({
    props: args,
  }),
  args: {
  },
}

export const YesNo = {
  render: (args: SwitchComponent) => ({
    props: args,
  }),
  args: {
    trueValue: 'Yes',
    falseValue: 'No',
  },
}