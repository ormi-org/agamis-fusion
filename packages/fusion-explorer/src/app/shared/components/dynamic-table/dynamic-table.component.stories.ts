import { Meta, moduleMetadata } from '@storybook/angular';
import { DynamicTableComponent } from './dynamic-table.component';
import { Column } from './models/column.model';
import { DUMMIES, DummyDatasource } from './.storybook/.model';
import { SharedModule } from '@shared/shared.module';
import { customViewport } from './.storybook';
import { MINIMAL_VIEWPORTS } from '@storybook/addon-viewport';
import { Ordering } from '@shared/constants/utils/ordering';

export default {
  title: 'Shared/DynamicTable',
  component: DynamicTableComponent,
  parameters: {
    viewport: {
      viewports: {
        ...customViewport,
        ...MINIMAL_VIEWPORTS
      },
      defaultViewport: 'shared-dyntable-default'
    }
  },
  decorators: [
    moduleMetadata({
      imports: [SharedModule]
    })
  ]
} as Meta<DynamicTableComponent<any>>;

export const Empty = {
  render: (args: DynamicTableComponent<any>) => ({
    props: args,
    template: `
      <shared-dyntable [datasource]="datasource">
        <shared-dyntable-column
          key="first"
          [title]="'a first column'"
          [resizable]="true"
          [initOrder]="initOrder">
        </shared-dyntable-column>
        <shared-dyntable-column
          key="second"
          [title]="'a second column'"
          [resizable]="true">
        </shared-dyntable-column>
        <shared-dyntable-column
          key="third"
          [title]="'a third column'"
          [resizable]="true">
        </shared-dyntable-column>
        <shared-dyntable-column
          key="fourth"
          [title]="'a fourth column'"
          [resizable]="false">
        </shared-dyntable-column>
      </shared-dyntable>
    `
  }),
  args: {
    datasource: DummyDatasource.asSourceOf(DUMMIES),
    initOrder: Ordering.ASC,
  },
};
