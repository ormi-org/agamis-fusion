import { Meta, moduleMetadata } from '@storybook/angular';
import { DynamicTableComponent } from './dynamic-table.component';
import { Column } from './models/column.model';
import { DummyDatasource } from './.storybook/.model';
import { SharedModule } from '@shared/shared.module';

export default {
  title: 'Shared/DynamicTable',
  component: DynamicTableComponent,
  decorators: [
    moduleMetadata({
      imports: [SharedModule]
    })
  ]
} as Meta<DynamicTableComponent<any>>;

export const Empty = {
  render: (args: DynamicTableComponent<any>) => ({
    props: args,
  }),
  args: {
    columns: [
      new Column("A first column", true),
      new Column("A second column", true),
      new Column("A third column", true),
      new Column("A fourth column", false),
    ],
    datasource: new DummyDatasource()
  },
};
