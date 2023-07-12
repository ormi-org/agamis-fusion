import { Meta, moduleMetadata } from '@storybook/angular'
import { SharedModule } from '@shared/shared.module'
import { customViewport } from './.storybook'
import { MINIMAL_VIEWPORTS } from '@storybook/addon-viewport'
import { SeparatorComponent } from './separator.component'
import { Direction } from './models/enums/direction.enum'
import { Color } from '@shared/constants/assets'

export default {
  title: 'Shared/Separator',
  component: SeparatorComponent,
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
} as Meta<SeparatorComponent>

export const Vertical = {
  render: (args: SeparatorComponent) => ({
    props: args,
  }),
  args: {
    direction: Direction.VERTICAL,
    thickness: 1,
    color: Color.SECONDARY_ONE,
    bordering: 5,
  },
}