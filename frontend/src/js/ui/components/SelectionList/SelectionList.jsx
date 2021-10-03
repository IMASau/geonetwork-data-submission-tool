import React from 'react';
import PropTypes from 'prop-types';
import {DragDropContext, Draggable, Droppable} from "react-beautiful-dnd";
import {useCachedState} from "../utils";

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
export function SelectionList({items, itemProps, getValue, onReorder, disabled, renderItem}) {

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
                                        {renderItem({...itemProps, item, index})}
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
    renderItem: PropTypes.func.isRequired,
    getValue: PropTypes.func.isRequired,
    disabled: PropTypes.bool,
}

export function SimpleListItem({item, getLabel}) {
    return (
        <div className="SimpleListItem">
            <div className="SimpleListItemLabel">{getLabel(item)}</div>
        </div>
    )
}

export function BreadcrumbListItem({item, getBreadcrumb, getLabel}) {
    return (
        <div className="BreadcrumbListItem">
            <div>
                <div className="BreadcrumbListItemPath">{getBreadcrumb(item)}</div>
                <div className="BreadcrumbListItemText">{getLabel(item)}</div>
            </div>
        </div>
    )
}

export function TableListItem({item, index, columns}) {
    return (
        <div className="TableListItemRow">
            {columns.map(({getLabel, flex}, columnIndex) =>
                <span className="TableListItemCol" style={{"flex": flex}}>
                    {getLabel(item)}
                </span>
            )}
        </div>
    )
}

SimpleListItem.propTypes = {
    item: PropTypes.shape({
        value: PropTypes.string,
        label: PropTypes.string
    }),
}
