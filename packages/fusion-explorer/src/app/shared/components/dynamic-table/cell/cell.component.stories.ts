import { Meta } from '@storybook/angular';
import { CellComponent } from './cell.component';

export default {
  title: 'CellComponent',
  component: CellComponent,
} as Meta<CellComponent>;

export const Primary = {
  render: (args: CellComponent) => ({
    props: args,
  }),
  args: {},
};
