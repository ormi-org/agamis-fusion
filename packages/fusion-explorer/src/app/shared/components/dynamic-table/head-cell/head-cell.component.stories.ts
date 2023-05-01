import { Meta, moduleMetadata } from '@storybook/angular';
import { HeadCellComponent } from './head-cell.component';
import { Observable } from 'rxjs';
import { HeadCellDefinition } from '../typed/head-cell-definition.interface';
import { MINIMAL_VIEWPORTS } from '@storybook/addon-viewport';
import { customViewport } from '../.storybook';
import { SharedModule } from '@shared/shared.module';
import { Ordering } from '@shared/constants/utils/ordering';

export default {
  title: 'Shared/DynamicTable/HeadCell',
  component: HeadCellComponent,
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
} as Meta<HeadCellComponent>;

export const Default = {
  render: (args: HeadCellComponent) => ({
    props: args,
  }),
  args: {
    associatedColumn: {
      key: "default",
      resizable: false,
      value: "a default test head cell",
      ordering: Ordering.NONE
    }
  },
};

export const OrderedAscending = {
  render: (args: HeadCellComponent) => ({
    props: args,
  }),
  args: {
    associatedColumn: {
      key: "default",
      resizable: false,
      value: "an ascending test head cell",
      ordering: Ordering.ASC
    }
  },
};

export const OrderedDescending = {
  render: (args: HeadCellComponent) => ({
    props: args,
  }),
  args: {
    associatedColumn: {
      key: "default",
      resizable: false,
      value: "a descending test head cell",
      ordering: Ordering.DESC
    }
  },
};
