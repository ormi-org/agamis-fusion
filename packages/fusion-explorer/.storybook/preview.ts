import { Meta } from '@storybook/angular';
import '@angular/localize/init';

const meta: Meta = {
    parameters: {
        options: {
            storySort: {
              order: ['Admin', 'Shared']
            }
        }
    }
}