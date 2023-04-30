import { Meta, moduleMetadata } from '@storybook/angular';
import { RowComponent } from './row.component';
import { DUMMIES } from '../.storybook/.model';
import { SharedModule } from '@shared/shared.module';
import { customViewport } from '../.storybook';
import { MINIMAL_VIEWPORTS } from '@storybook/addon-viewport';

export default {
  title: 'Shared/DynamicTable/Row',
  component: RowComponent,
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
} as Meta<RowComponent<any>>;

export const Default = {
  render: (args: RowComponent<any>) => ({
    props: args,
  }),
  args: {
    index: 0,
    model: DUMMIES[1]
  },
};

export const DefaultSelected = {
  render: (args: RowComponent<any>) => ({
    props: args,
  }),
  args: {
    selected: true,
    index: 0,
    model: DUMMIES[1]
  },
};
