import { Meta, moduleMetadata } from '@storybook/angular';
import { MenuComponent } from './menu.component';
import { MINIMAL_VIEWPORTS } from '@storybook/addon-viewport';
import { customViewport } from './.storybook';
import { SharedModule } from '@shared/shared.module';
import { ItemComponent } from './item/item.component';

export default {
  title: 'MenuComponent',
  component: MenuComponent,
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
      imports: [SharedModule],
      declarations: [ItemComponent]
    })
  ]
} as Meta<MenuComponent>;

export const Default = {
  render: (args: MenuComponent) => ({
    props: args,
  }),
  args: {
    text: "administration"
  }
};

export const Expanded = {
  render: (args: MenuComponent) => ({
    props: args,
  }),
  args: {
    text: "administration",
    isCollapsed: false
  }
};
