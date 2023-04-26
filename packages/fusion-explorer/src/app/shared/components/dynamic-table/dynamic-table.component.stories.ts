import { Meta } from '@storybook/angular';
import { DynamicTableComponent } from './dynamic-table.component';

export default {
  title: 'DynamicTableComponent',
  component: DynamicTableComponent,
} as Meta<DynamicTableComponent>;

export const Empty = {
  render: (args: DynamicTableComponent) => ({
    props: args,
  }),
  args: {},
};
