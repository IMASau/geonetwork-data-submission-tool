import React from 'react';

import {DropdownMenu} from './DropdownMenu';
import './DropdownMenu.css';
import '@blueprintjs/core/lib/css/blueprint.css';

export default {
    title: 'Example/DropdownMenu',
    component: DropdownMenu,
    argTypes: {
    }
};

const FieldTemplate = (args) => <DropdownMenu {...args} />;

export const Example = FieldTemplate.bind({});
Example.args = {
    text: "Add",
    icon: "add",
    menuItems: [
        {
            text: "Create a new person",
            icon: "add",
            onClick: () => console.log("Opening \"create a new person\" dialogue")
        },
        {divider: true},
        {
            text: "Bill Bobson",
            onClick: () => console.log("Cloning: Bill Bobson")
        },
        {
            text: "Bob Billson",
            onClick: () => console.log("Cloning: Bob Billson")
        }]
};