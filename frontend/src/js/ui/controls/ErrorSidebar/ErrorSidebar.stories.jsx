import React from 'react';

import {ErrorSidebar} from './ErrorSidebar';
import './ErrorSidebar.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import NOTES from './NOTES.mdx';

export default {
    title: 'Example/ErrorSidebar',
    component: ErrorSidebar,
    argTypes: {
        // Most are inferred from propTypes
    }
};

const Template = (args) => <ErrorSidebar {...args} />;

export const SimpleField = Template.bind({});
SimpleField.args = {
    labels: ["Topic categories", "Status of data", "Version", "Maintenance/Update freq"],
};

export const NoErrors = Template.bind({});
NoErrors.args = {
    labels: []
};


export const DesignDecisions = NOTES;
