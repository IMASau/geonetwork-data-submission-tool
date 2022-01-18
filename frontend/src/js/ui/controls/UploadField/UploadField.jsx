import React, { useCallback } from 'react'
import PropTypes from 'prop-types';
import { useDropzone } from 'react-dropzone'


export function Dropzone({ disabled, onDrop, placeholder, maxFiles, maxSize }) {
    const handleDrop = useCallback((acceptedFiles, rejectedFiles) => {
        // Do something with the files
        onDrop({ acceptedFiles, rejectedFiles })
    }, [])
    const {getRootProps, getInputProps} = useDropzone({onDrop: handleDrop, disabled, maxFiles, maxSize});

    return (
        <div>
            <div {...getRootProps({ className: 'dropzone' })}>
                <input {...getInputProps()} />
                {placeholder}
                </div>
        </div>
    )
}

Dropzone.propTypes = {
    disabled: PropTypes.bool,
    onDrop: PropTypes.func,
    maxFiles: PropTypes.number,
    maxSize: PropTypes.number,
    placeholder: PropTypes.node.isRequired
}

export function UploadField({ disabled, onDropFile }) {
    return (
        <Dropzone 
            disabled={disabled} 
            onDropFile={onDropFile} />
    );
}

UploadField.propTypes = {
    disabled: PropTypes.bool,
    onDropFile: PropTypes.func,
}
