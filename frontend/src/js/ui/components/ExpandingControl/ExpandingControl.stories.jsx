import React from 'react';

import {ExpandingControl} from './ExpandingControl';
import './ExpandingControl.css';
import '@blueprintjs/core/lib/css/blueprint.css';


export default {
    title: 'Example/ExpandingControl',
    component: ExpandingControl,
    argTypes: {
        // Most are inferred from propTypes
    }
};

const FieldTemplate = (args) => <ExpandingControl {...args} />;

export const Defaults = FieldTemplate.bind({});
Defaults.args = {
    label: "A label",
    children: "This is the content"
};

const FieldTemplate2 = (args) => <div>
    <ExpandingControl {...args} />
    <ExpandingControl {...args} />
</div>;

export const Defaults2 = FieldTemplate2.bind({});
Defaults2.args = {
    label: "A label",
    children: "This is the content"
};

import NOTES from './NOTES.mdx';
export const DesignDecisions = NOTES;