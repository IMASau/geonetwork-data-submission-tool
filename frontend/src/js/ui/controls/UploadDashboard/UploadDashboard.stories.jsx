import React from 'react';
import UploadDashboard from './UploadDashboard';
import '@uppy/core/dist/style.css'
import '@uppy/dashboard/dist/style.css'
import '@uppy/drag-drop/dist/style.css'
import '@uppy/file-input/dist/style.css'
import '@uppy/progress-bar/dist/style.css'

export default {
    title: 'Example/UploadDashboard',
    component: UploadDashboard,
    argTypes: {
    }
};

const FieldTemplate = (args) => <UploadDashboard {...args} />;

export const Example = FieldTemplate.bind({});
Example.args = {};
