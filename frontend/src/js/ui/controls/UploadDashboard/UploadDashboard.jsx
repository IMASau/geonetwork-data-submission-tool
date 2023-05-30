import React from 'react'
import Uppy from '@uppy/core'
import { Dashboard } from '@uppy/react'
import Tus from '@uppy/tus'
// import GoogleDrive from'@uppy/google-drive' // If this line is uncommented, @uppy/provider-views complains that it cannot find the required dependency p-queue

export default class UploadDashboard extends React.Component {
    constructor(props) {
        super(props)

        this.uppy = new Uppy({ id: 'uppy', autoProceed: true, debug: true })
            .use(Tus, { endpoint: 'http://localhost:1080/files' })
            // .use(GoogleDrive, { companionUrl: 'http://localhost:3020/' })
            .on('upload-success', e => console.log(e));
    }

    componentWillUnmount() {
        this.uppy.close({ reason: 'unmount' })
    }

    render() {
        return (
            <Dashboard
                uppy={this.uppy}
                // plugins={['GoogleDrive']}
                metaFields={[
                    { id: 'name', name: 'Name', placeholder: 'File name' }
                ]}
            />
        );
    }
}

