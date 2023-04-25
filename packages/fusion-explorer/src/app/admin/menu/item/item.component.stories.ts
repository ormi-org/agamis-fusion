import { Meta, moduleMetadata } from '@storybook/angular';
import { SharedModule } from '@shared/shared.module';
import { ItemComponent } from './item.component';
import { customViewport } from '../.storybook';
import { MINIMAL_VIEWPORTS } from '@storybook/addon-viewport';

export default {
  title: 'ItemComponent',
  component: ItemComponent,
  parameters: {
    viewport: {
      viewports: {
        ...customViewport,
        ...MINIMAL_VIEWPORTS
      },
      defaultViewport: 'admin-menu-default'
    }
  },
  decorators: [
    moduleMetadata({
      imports: [SharedModule]
    })
  ]
} as Meta<ItemComponent>;

export const itemNormal = {  
  render: (args: ItemComponent) => ({
    props: args,
  }),
  args: {
    isActive: false,
    text: "a normal menu item"
  },
};

export const itemActive = {
  render: (args: ItemComponent) => ({
    props: args,
  }),
  args: {
    isActive: true,
    text: "an active menu item"
  },
};
