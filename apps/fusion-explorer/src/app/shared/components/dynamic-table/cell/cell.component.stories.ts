import { SharedModule } from '@shared/shared.module'
import { MINIMAL_VIEWPORTS } from '@storybook/addon-viewport'
import { Meta, moduleMetadata } from '@storybook/angular'
import { BehaviorSubject } from 'rxjs'
import { customViewport } from '../.storybook'
import { CellComponent } from './cell.component'

export default {
  title: 'Shared/DynamicTable/Cell',
  component: CellComponent,
  parameters: {
    viewport: {
      viewports: {
        ...customViewport,
        ...MINIMAL_VIEWPORTS
      },
      defaultViewport: 'shared-dyntable-subs'
    }
  },
  decorators: [
    moduleMetadata({
      imports: [SharedModule]
    })
  ]
} as Meta<CellComponent>

export const Default = {
  render: (args: CellComponent) => ({
    props: args,
  }),
  args: {
    value: "a default test cell",
    widthSubject: new BehaviorSubject(100)
  },
}
