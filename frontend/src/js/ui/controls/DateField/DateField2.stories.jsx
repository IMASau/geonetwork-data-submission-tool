import React from 'react';

import {DateField2} from './DateField2';
import './DateField2.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import 'react-day-picker/lib/style.css';
import NOTES from './NOTES.mdx';

export default {
    title: 'Example/DateField2',
    component: DateField2,
    argTypes: {
        // Most are inferred from DateField2.propTypes
        onChange: {action: 'onChange'},
    }
};

const Template = (args) => <DateField2 {...args} />;

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

export const JunkIn = Template.bind({});
JunkIn.args = {
    value: "foobar",
    disabled: false,
    hasError: false,
};

export const Change = (args) => {
    const [value, setValue] = React.useState(args.value);
    return <DateField2 {...args} value={value} onChange={(v, t) => setValue(v)}/>;
};
Change.args = {
    value: "1975-11-03",
    disabled: false,
    hasError: false,
};


export const DesignDecisions = NOTES;
