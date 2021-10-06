import React from 'react';
import PropTypes from 'prop-types';
import {DragDropContext, Draggable, Droppable} from "react-beautiful-dnd";
import {useCachedState} from "../utils";
import * as ReactDOM from "react-dom";

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

// NOTE: Workaround for conflict with blueprint expander (transform related?)
// https://github.com/vtereshyn/react-beautiful-dnd-ru/blob/master/docs/patterns/using-a-portal.md
const portal = document.createElement('div');
portal.classList.add('SelectionListPortal');

if (!document.body) {
    throw new Error('body not ready for portal creation!');
} else {
    document.body.appendChild(portal);
}

function PortalAwareItem({provided, snapshot, children}) {

    const usePortal = snapshot.isDragging;

    const child = (
        <div
            ref={provided.innerRef}
            {...provided.draggableProps}
            {...provided.dragHandleProps}
            style={getItemStyle(
                snapshot.isDragging,
                provided.draggableProps.style
            )}
        >
            {children}
        </div>
    );

    if (!usePortal) {
        return child;
    } else {
        return ReactDOM.createPortal(child, portal);
    }
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
                                    <PortalAwareItem
                                        provided={provided}
                                        snapshot={snapshot}
                                    >
                                        {renderItem({...itemProps, onRemoveClick, item, index})}
                                    </PortalAwareItem>
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
