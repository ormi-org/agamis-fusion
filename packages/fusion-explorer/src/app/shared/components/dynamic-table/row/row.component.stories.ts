import { Meta, moduleMetadata } from '@storybook/angular';
import { RowComponent } from './row.component';
import { DUMMIES } from '../.storybook/.model';
import { SharedModule } from '@shared/shared.module';
import { CellComponent } from '../cell/cell.component';

export default {
  title: 'Shared/DynamicTable/Row',
  component: RowComponent,
  decorators: [
    moduleMetadata({
      imports: [SharedModule]
    })
  ]
} as Meta<RowComponent<any>>;

export const Empty = {
  render: (args: RowComponent<any>) => ({
    props: args,
  }),
  args: {
    index: 0,
    model: DUMMIES[1]
  },
};

export const EmptySelected = {
  render: (args: RowComponent<any>) => ({
    props: args,
  }),
  args: {
    selected: true,
    index: 0,
    model: DUMMIES[1]
  },
};
