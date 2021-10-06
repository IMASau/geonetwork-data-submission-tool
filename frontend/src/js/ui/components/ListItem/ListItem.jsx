import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from '@blueprintjs/core'

function RemoveButton({ disabled, onClick }) {
    return (
        <BPCore.Button icon="cross"
            small={true}
            minimal={true}
            disabled={disabled}
            onClick={onClick} />
    )
}

export function SimpleListItem({ item, disabled, getLabel, onRemoveClick }) {
    return (
        <div className="SimpleListItem">
            <div className="SimpleListItemLabel">{getLabel(item)}</div>
            {onRemoveClick ?
                <div className="SimpleListItemRemove">
                    <RemoveButton disabled={disabled} onClick={onRemoveClick} />
                </div> :
                null}
        </div>
    )
}

SimpleListItem.propTypes = {
    item: PropTypes.object,
    disabled: PropTypes.bool,
    getLabel: PropTypes.func,
    onRemoveClick: PropTypes.func
}

export function BreadcrumbListItem({ item, disabled, getBreadcrumb, getLabel, onRemoveClick }) {
    return (
        <div className="BreadcrumbListItem">
            <div className="BreadcrumbListItemLabel">
                <div className="BreadcrumbListItemPath">{getBreadcrumb(item)}</div>
                <div className="BreadcrumbListItemText">{getLabel(item)}</div>
            </div>
            {onRemoveClick ?
                <div className="BreadcrumbListItemRemove">
                    <RemoveButton disabled={disabled} onClick={onRemoveClick} />
                </div> :
                null}
        </div>
    )
}

BreadcrumbListItem.propTypes = {
    item: PropTypes.shape({
        item: PropTypes.object,
        disabled: PropTypes.bool,
        getLabel: PropTypes.func,
        getBreadcrumb: PropTypes.func,
        onRemoveClick: PropTypes.func
    }),
}

function TableListItemCol({ item, flex, getLabel }) {
    return (
        <span className="TableListItemCol" style={{ "flex": flex }}>
            {getLabel(item)}
        </span>
    )
}

export function TableListItem({ item, disabled, columns, onRemoveClick }) {
    return (
        <div className="TableListItemRow">
            {columns.map((columnProps, columnIndex) =>
                <TableListItemCol key={"col"+columnIndex} item={item} {...columnProps} />
            )}
            {onRemoveClick ?
                <span className="TableListItemRemove">
                    <RemoveButton onClick={() => onRemoveClick()} />
                </span> :
                null}
        </div>
    )
}

TableListItem.propTypes = {
    item: PropTypes.object,
    disabled: PropTypes.bool,
    columns: PropTypes.arrayOf(PropTypes.shape({
        getLabel: PropTypes.func,
        flex: PropTypes.number
    })),
    onRemoveClick: PropTypes.func
}
