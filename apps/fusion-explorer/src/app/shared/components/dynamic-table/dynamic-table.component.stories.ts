import { Meta, moduleMetadata } from '@storybook/angular'
import { DynamicTableComponent } from './dynamic-table.component'
import { DUMMIES, DummyDatasource } from './.storybook/.model'
import { SharedModule } from '@shared/shared.module'
import { customViewport } from './.storybook'
import { MINIMAL_VIEWPORTS } from '@storybook/addon-viewport'
import { Ordering } from '@shared/constants/utils/ordering'
import { Uniquely } from './typed/uniquely.interface'

export default {
  title: 'Shared/DynamicTable/Table',
  component: DynamicTableComponent,
  parameters: {
    viewport: {
      viewports: {
        ...customViewport,
        ...MINIMAL_VIEWPORTS,
      },
      defaultViewport: 'shared-dyntable-default',
    },
  },
  decorators: [
    moduleMetadata({
      imports: [SharedModule],
    }),
  ],
} as Meta<DynamicTableComponent<Uniquely>>

export const Default = {
  render: (args: DynamicTableComponent<Uniquely>) => ({
    props: args,
    template: `
      <shared-dyntable [datasource]="datasource">
        <shared-dyntable-column
          key="first"
          [title]="'a first column'"
          [resizable]="true"
          [initOrder]="initOrder"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="second"
          [title]="'a second column'"
          [resizable]="true"
          [width]=140>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="third"
          [title]="'a third column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="fourth"
          [title]="'a fourth column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
      </shared-dyntable>
    `,
  }),
  args: {
    datasource: DummyDatasource.asSourceOf(DUMMIES),
    initOrder: Ordering.ASC,
  },
}

export const OrderedBySecondColumn = {
  render: (args: DynamicTableComponent<Uniquely>) => ({
    props: args,
    template: `
      <shared-dyntable [datasource]="datasource">
        <shared-dyntable-column
          key="first"
          [title]="'a first column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="second"
          [title]="'a second column'"
          [resizable]="true"
          [initOrder]="initOrder"
          [width]=140>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="third"
          [title]="'a third column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="fourth"
          [title]="'a fourth column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
      </shared-dyntable>
    `,
  }),
  args: {
    datasource: DummyDatasource.asSourceOf(DUMMIES),
    initOrder: Ordering.DESC,
  },
}

export const Empty = {
  render: (args: DynamicTableComponent<Uniquely>) => ({
    props: args,
    template: `
      <shared-dyntable
        [datasource]="datasource">
        <shared-dyntable-column
          key="first"
          [title]="'a first column'"
          [resizable]="true"
          [initOrder]="initOrder"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="second"
          [title]="'a second column'"
          [resizable]="true"
          [width]=140>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="third"
          [title]="'a third column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="fourth"
          [title]="'a fourth column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
      </shared-dyntable>
    `,
  }),
  args: {
    datasource: DummyDatasource.asSourceOf([]),
    initOrder: Ordering.ASC,
  },
}

export const EmptyWithCustomHint = {
  render: (args: DynamicTableComponent<Uniquely>) => ({
    props: args,
    template: `
      <shared-dyntable
        [datasource]="datasource"
        [emptyHint]="'This is a message to replace empty result'">
        <shared-dyntable-column
          key="first"
          [title]="'a first column'"
          [resizable]="true"
          [initOrder]="initOrder"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="second"
          [title]="'a second column'"
          [resizable]="true"
          [width]=140>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="third"
          [title]="'a third column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="fourth"
          [title]="'a fourth column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
      </shared-dyntable>
    `,
  }),
  args: {
    datasource: DummyDatasource.asSourceOf([]),
    initOrder: Ordering.ASC,
  },
}

export const MissingColumn = {
  render: (args: DynamicTableComponent<Uniquely>) => ({
    props: args,
    template: `
      <shared-dyntable [datasource]="datasource">
        <shared-dyntable-column
          key="first"
          [title]="'a first column'"
          [resizable]="true"
          [initOrder]="initOrder"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="third"
          [title]="'a third column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="fourth"
          [title]="'a fourth column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
      </shared-dyntable>
    `,
  }),
  args: {
    datasource: DummyDatasource.asSourceOf(DUMMIES),
    initOrder: Ordering.ASC,
  },
}

export const FilteredColumn = {
  render: (args: DynamicTableComponent<Uniquely>) => ({
    props: args,
    template: `
      <shared-dyntable [datasource]="datasource">
        <shared-dyntable-column
          key="first"
          [title]="'a first column'"
          [resizable]="true"
          [initOrder]="initOrder"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="second"
          [title]="'a second column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="third"
          [title]="'a third column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="fourth"
          [title]="'a fourth column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
      </shared-dyntable>
    `,
  }),
  args: {
    datasource: DummyDatasource.asSourceOf(DUMMIES),
    initOrder: Ordering.ASC,
  },
}

export const EllipsedText = {
  render: (args: DynamicTableComponent<Uniquely>) => ({
    props: args,
    template: `
      <shared-dyntable [datasource]="datasource">
        <shared-dyntable-column
          key="first"
          [title]="'a first column'"
          [resizable]="true"
          [initOrder]="initOrder"
          [width]=130>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="second"
          [title]="'a second column'"
          [resizable]="true"
          [width]=50>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="third"
          [title]="'a third column'"
          [resizable]="true"
          [width]=80>
        </shared-dyntable-column>
        <shared-dyntable-column
          key="fourth"
          [title]="'a fourth column'"
          [resizable]="true"
          [width]=120>
        </shared-dyntable-column>
      </shared-dyntable>
    `,
  }),
  args: {
    datasource: DummyDatasource.asSourceOf(DUMMIES),
    initOrder: Ordering.ASC,
  },
}
