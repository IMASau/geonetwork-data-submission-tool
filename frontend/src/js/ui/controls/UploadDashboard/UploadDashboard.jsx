import React from 'react'
import Uppy from '@uppy/core'
import { Dashboard } from '@uppy/react'
import Tus from '@uppy/tus'
import GoogleDrive from '@uppy/google-drive'

export default class UploadDashboard extends React.Component {
    constructor(props) {
        super(props);
        const { tusUrl, companionUrl, onUploadSuccess, metadata } = props;

        this.uppy = new Uppy({
            id: 'uppy',
            autoProceed: true,
            debug: true,
            onBeforeUpload(files) {
                for (const [_, file] of Object.entries(files)) {
                    for (const [key, value] of Object.entries(metadata)) {
                        file.meta[key] = value;
                    }
                }
            }
        })
            .use(Tus, { endpoint: tusUrl })
            .use(GoogleDrive, { companionUrl: companionUrl })
            .on('upload-success', onUploadSuccess);
    }

    componentWillUnmount() {
        this.uppy.close({ reason: 'unmount' })
    }

    render() {
        return (
            <Dashboard
                uppy={this.uppy}
                plugins={['GoogleDrive']}
                width='100%'
                metaFields={[
                    { id: 'name', name: 'Name', placeholder: 'File name' }
                ]}
            />
        );
    }
}

