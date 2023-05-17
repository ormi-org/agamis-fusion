import { Meta } from '@storybook/angular';
import { UsersComponent } from './users.component';

export default {
  title: 'UsersComponent',
  component: UsersComponent,
} as Meta<UsersComponent>;

export const Primary = {
  render: (args: UsersComponent) => ({
    props: args,
  }),
  args: {},
};
