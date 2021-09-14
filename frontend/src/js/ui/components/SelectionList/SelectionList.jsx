import React from 'react';
import PropTypes from 'prop-types';


export function SelectionListItem(value) {
    return <li>{value.label}</li>
}

export function SelectionList({values}) {
    return (
        <ul>
            {values.map(SelectionListItem)}
        </ul>
    );
}

SelectionList.propTypes = {
    values: PropTypes.arrayOf(PropTypes.object),
}
