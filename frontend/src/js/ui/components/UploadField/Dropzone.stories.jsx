import React from 'react';

import {Dropzone} from './UploadField';
import './UploadField.css';
import '@blueprintjs/core/lib/css/blueprint.css';

export default {
    title: 'Example/Dropzone',
    component: Dropzone,
    argTypes: {
        // Most are inferred from propTypes
        onDropFile: {action: 'onDropFile'},
    }
};

const FieldTemplate = (args) => <Dropzone {...args} />;

export const SimpleDropzone = FieldTemplate.bind({});
SimpleDropzone.args = {
    disabled: false
};
