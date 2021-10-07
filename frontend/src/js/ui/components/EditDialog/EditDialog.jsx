import * as React from 'react';
import PropTypes from 'prop-types';
import {Button, Classes, Dialog, Intent} from "@blueprintjs/core";

EditDialog.propTypes = {
    title: PropTypes.string.isRequired,
    body: PropTypes.elementType.isRequired,
    onClear: PropTypes.func.isRequired,
    onSave: PropTypes.func.isRequired,
    canSave: PropTypes.bool,
}

export function EditDialog({title, body, onClear, onSave, canSave}) {
    const [isOpen, setOpen] = React.useState(false);
    function open() {setOpen(true)}
    function close() {setOpen(false)}
    function clear() {close(); onClear()}
    function save() {close(); onSave()}
    return (
        <div>
            <Button onClick={open}>Show dialog</Button>
            <Dialog
                icon="info-sign"
                onClose={close}
                title={title}
                canEscapeKeyClose={false}
                canOutsideClickClose={false}
                isOpen={isOpen}
                usePortal={true}
                backdropClassName="EditDialogBackdrop"
            >
                <div className={Classes.DIALOG_BODY}>
                    {body}
                </div>
                <div className={Classes.DIALOG_FOOTER}>
                    <div className={Classes.DIALOG_FOOTER_ACTIONS}>
                        <Button onClick={clear}>Clear</Button>
                        <Button onClick={save} disabled={canSave} intent={Intent.PRIMARY}>Save</Button>
                    </div>
                </div>
            </Dialog>
        </div>
    )
}