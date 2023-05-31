import React from 'react'
import Uppy from '@uppy/core'
import { Dashboard } from '@uppy/react'
import Tus from '@uppy/tus'
import GoogleDrive from'@uppy/google-drive'

export default class UploadDashboard extends React.Component {
    constructor(props) {
        super(props);
        const { tusUrl, companionUrl, onUploadSuccess } = props;

        this.uppy = new Uppy({ id: 'uppy', autoProceed: true, debug: true })
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
                metaFields={[
                    { id: 'name', name: 'Name', placeholder: 'File name' }
                ]}
            />
        );
    }
}

