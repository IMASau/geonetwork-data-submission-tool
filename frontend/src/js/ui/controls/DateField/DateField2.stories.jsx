import React from 'react';

import {DateField2} from './DateField2';
import './DateField2.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import NOTES from './NOTES.mdx';

export default {
    title: 'Example/DateField2',
    component: DateField2,
    argTypes: {
        // Most are inferred from DateField2.propTypes
        value: {control: 'date'},
        onChange: {action: 'onChange'},
    }
};

// Wrapper helps because
const Wrapper = (args) => {
    const value = args.value ? new Date(args.value) : null;

    return <DateField2 {...args} value={value}/>;
};

const Template = (args) => <Wrapper {...args} />;

export const Empty = Template.bind({});
Empty.args = {
    disabled: false,
    hasError: false,
};

export const Preset = Template.bind({});
Preset.args = {
    value: "1975-11-03",
    disabled: false,
    hasError: false,
};

export const Change = (args) => {
    const [value, setValue] = React.useState(args.value);
    return <Wrapper {...args} value={value} onChange={(v, t) => setValue(v)}/>;
};
Change.args = {
    value: "1975-11-03",
    disabled: false,
    hasError: false,
};


export const DesignDecisions = NOTES;
