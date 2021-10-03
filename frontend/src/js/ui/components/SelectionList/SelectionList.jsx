import React from 'react';
import PropTypes from 'prop-types';
import {DragDropContext, Draggable, Droppable} from "react-beautiful-dnd";
import {useCachedState} from "../utils";
import * as BPCore from "@blueprintjs/core";

const getItemStyle = (isDragging, draggableStyle) => ({
    // some basic styles to make the items look a bit nicer
    userSelect: "none",

    // change background colour if dragging
    background: isDragging ? "rgba(.6,0.6,0.6,.25)" : null,

    // styles we need to apply on draggables
    ...draggableStyle
});

const getListStyle = isDraggingOver => ({
    background: isDraggingOver ? "lightblue" : null,
});


function reorder(list, startIndex, endIndex) {
    const result = Array.from(list);
    const [removed] = result.splice(startIndex, 1);
    result.splice(endIndex, 0, removed);
    return result;
}

// NOTE: Attempts to workaround glitch on recorder by caching state
// NOTE: Component should change key to flush invalid state if necessary
export function SelectionList({items, itemProps, getValue, onReorder, onRemoveClick, disabled, renderItem}) {

    const [stateValue, setStateValue] = useCachedState(items);
    const isDragDisabled = disabled || !onReorder;

    const onDragEnd = (result) => {
        if (result.destination) {
            // Optimistially reorder to avoid glitch
            setStateValue(reorder(
                stateValue,
                result.source.index,
                result.destination.index
            ))
            onReorder(
                result.source.index,
                result.destination.index
            )
        }
    }

    return (
        <DragDropContext onDragEnd={onDragEnd}>
            <Droppable droppableId="droppable">
                {(provided, snapshot) => (
                    <div
                        {...provided.droppableProps}
                        ref={provided.innerRef}
                        style={getListStyle(snapshot.isDraggingOver)}
                    >
                        {stateValue.map((item, index) => (
                            <Draggable key={getValue(item)}
                                       draggableId={getValue(item)}
                                       index={index}
                                       isDragDisabled={isDragDisabled}>
                                {(provided, snapshot) => (
                                    <div
                                        ref={provided.innerRef}
                                        {...provided.draggableProps}
                                        {...provided.dragHandleProps}
                                        style={getItemStyle(
                                            snapshot.isDragging,
                                            provided.draggableProps.style
                                        )}
                                    >
                                        {renderItem({...itemProps, onRemoveClick, item, index})}
                                    </div>
                                )}
                            </Draggable>
                        ))}
                        {provided.placeholder}
                    </div>
                )}
            </Droppable>
        </DragDropContext>
    )
}

SelectionList.propTypes = {
    items: PropTypes.arrayOf(PropTypes.shape({
        value: PropTypes.string,
        label: PropTypes.string
    })),
    onReorder: PropTypes.func,
    onRemoveClick: PropTypes.func,
    renderItem: PropTypes.func.isRequired,
    getValue: PropTypes.func.isRequired,
    disabled: PropTypes.bool,
}

function RemoveButton({disabled, onClick}) {
    return (
        <BPCore.Button icon="cross"
                       small={true}
                       minimal={true}
                       disabled={disabled}
                       onClick={onClick}/>
    )
}

export function SimpleListItem({item, index, disabled, getLabel, onRemoveClick}) {
    return (
        <div className="SimpleListItem">
            <div className="SimpleListItemLabel">{getLabel(item)}</div>
            <div className="SimpleListItemRemove">
                <RemoveButton disabled={disabled} onClick={() => onRemoveClick(index)}/>
            </div>
        </div>
    )
}

SimpleListItem.propTypes = {
    item: PropTypes.shape({
        item: PropTypes.object,
        index: PropTypes.number,
        disabled: PropTypes.bool,
        getLabel: PropTypes.func,
        onRemoveClick: PropTypes.func
    }),
}

export function BreadcrumbListItem({item, index, disabled, getBreadcrumb, getLabel, onRemoveClick}) {
    return (
        <div className="BreadcrumbListItem">
            <div className="BreadcrumbListItemLabel">
                <div className="BreadcrumbListItemPath">{getBreadcrumb(item)}</div>
                <div className="BreadcrumbListItemText">{getLabel(item)}</div>
            </div>
            <div className="BreadcrumbListItemRemove">
                <RemoveButton disabled={disabled} onClick={() => onRemoveClick(index)}/>
            </div>
        </div>
    )
}

BreadcrumbListItem.propTypes = {
    item: PropTypes.shape({
        item: PropTypes.object,
        index: PropTypes.number,
        disabled: PropTypes.bool,
        getLabel: PropTypes.func,
        getBreadcrumb: PropTypes.func,
        onRemoveClick: PropTypes.func
    }),
}

export function TableListItem({item, index, disabled, columns, onRemoveClick}) {
    return (
        <div className="TableListItemRow">
            {columns.map(({getLabel, flex}, columnIndex) =>
                <span className="TableListItemCol" style={{"flex": flex}}>
                    {getLabel(item)}
                </span>
            )}
            <span className="TableListItemRemove">
                <RemoveButton disabled={disabled} onClick={() => onRemoveClick(index)}/>
            </span>
        </div>
    )
}

TableListItem.propTypes = {
    item: PropTypes.shape({
        item: PropTypes.object,
        index: PropTypes.number,
        disabled: PropTypes.bool,
        columns: PropTypes.arrayOf(PropTypes.shape({
            getLabel: PropTypes.func,
            flex: PropTypes.number
        })),
        onRemoveClick: PropTypes.func
    }),
}
