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
} as Meta<RowComponent<object>>;

export const Default = {
  render: (args: RowComponent<object>) => ({
    props: args,
  }),
  args: {
    index: 0,
    keys: ["first", "second", "third", "fourth"],
    model: DUMMIES[1]
  },
};

export const DefaultSelected = {
  render: (args: RowComponent<object>) => ({
    props: args,
  }),
  args: {
    selected: true,
    index: 0,
    keys: ["first", "second", "third", "fourth"],
    model: DUMMIES[1]
  },
};
