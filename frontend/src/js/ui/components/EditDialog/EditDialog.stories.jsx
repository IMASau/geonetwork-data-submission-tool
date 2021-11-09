import React from 'react';

import {EditDialog} from './EditDialog';
import '@blueprintjs/core/lib/css/blueprint.css';
import './EditDialog.css';
import {Button} from '@blueprintjs/core';

export default {
    title: 'Example/EditDialog',
    component: EditDialog,
    argTypes: {
        onClose: {action: 'onClose'},
        onClear: {action: 'onClear'},
        onSave: {action: 'onSave'},
    }
};

const FieldTemplate = (args) => <EditDialog {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    isOpen: true,
    children: <div>Do you want to build a snowman?</div>
};

export const VeryLongDialog = FieldTemplate.bind({});
VeryLongDialog.args = {
    isOpen: true,
    children: Array(30).fill().map(x => <div>{x}. roar</div>)
};

function DialogButton() {
    const [isOpen, setOpen] = React.useState(false);

    function open() {
        setOpen(true)
    }

    function close() {
        setOpen(false)
    }
    return (
        <div>
            <Button onClick={open}>Open Dialog</Button>
            <EditDialog isOpen={isOpen}
                        title="Second dialog"
                        children="Another dialog!"
                        onClose={close}
                        onClear={close}
                        onSave={close}/>
        </div>
    )
}

export const DoubleDialog = FieldTemplate.bind({});
DoubleDialog.args = {
    isOpen: true,
    title: "First dialog",
    children: <DialogButton />
};
