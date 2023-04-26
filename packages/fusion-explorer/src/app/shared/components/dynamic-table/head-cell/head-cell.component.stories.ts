import { Meta, moduleMetadata } from '@storybook/angular';
import { HeadCellComponent } from './head-cell.component';
import { Observable } from 'rxjs';
import { HeadCellDefinition } from '../models/head-cell-definition.model';
import { MINIMAL_VIEWPORTS } from '@storybook/addon-viewport';
import { customViewport } from '../.storybook';
import { SharedModule } from '@shared/shared.module';

export default {
  title: 'HeadCellComponent',
  component: HeadCellComponent,
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
} as Meta<HeadCellComponent>;

export const Default = {
  render: (args: HeadCellComponent) => ({
    props: args,
  }),
  args: {
    def: new Observable((subscriber) => {
      subscriber.next(new HeadCellDefinition("a default test head cell"));
    })
  },
};
