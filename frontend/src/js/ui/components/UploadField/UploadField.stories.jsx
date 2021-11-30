import React from 'react';

import {UploadField} from './UploadField';
import './UploadField.css';
import '@blueprintjs/core/lib/css/blueprint.css';

export default {
    title: 'Example/UploadField',
    component: UploadField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const FieldTemplate = (args) => <UploadField {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
};
