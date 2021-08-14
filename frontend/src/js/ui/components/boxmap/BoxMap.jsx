import React from 'react';
import PropTypes from 'prop-types';
import { Map, TileLayer, FeatureGroup, Marker, Rectangle } from 'react-leaflet';
import { EditControl } from 'react-leaflet-draw';

function mapToBounds({ west, east, south, north }) {
    return [[south, west], [north, east]];
}

function geographicElementToPoint({ southBoundLatitude, westBoundLongitude }) {
    return [southBoundLatitude, westBoundLongitude];
}

function geographicElementIsPoint({ northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude }) {
    return ((northBoundLatitude == southBoundLatitude) &&
        (eastBoundLongitude == westBoundLongitude));
}

function geographicElementToBounds({ northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude }) {
    return [[southBoundLatitude, westBoundLongitude],
    [northBoundLatitude, eastBoundLongitude]];
}

function getMaxOfArray(numArray) {
    return Math.max.apply(null, numArray);
}

function getMinOfArray(numArray) {
    return Math.min.apply(null, numArray);
}

function boxesToExtents(boxes) {
    var north = getMaxOfArray(boxes.map((box) => box.northBoundLatitude));
    var west = getMinOfArray(boxes.map((box) => box.westBoundLongitude));
    var south = getMinOfArray(boxes.map((box) => box.southBoundLatitude));
    var east = getMaxOfArray(boxes.map((box) => box.eastBoundLongitude));
    return {
        "north": north,
        "west": west,
        "east": east,
        "south": south
    }
}

function geometryPointToData({ coordinates }) {
    const [lng, lat] = coordinates;
    return {
        northBoundLatitude: lat,
        southBoundLatitude: lat,
        eastBoundLongitude: lng,
        westBoundLongitude: lng
    }
}

function geometryPolygonToData({ coordinates }) {
    const [points] = coordinates;
    const lngs = points.map(([a, _]) => a);
    const lats = points.map(([_, b]) => b);
    return {
        northBoundLatitude: getMaxOfArray(lats),
        southBoundLatitude: getMinOfArray(lats),
        eastBoundLongitude: getMaxOfArray(lngs),
        westBoundLongitude: getMinOfArray(lngs)
    }
}

function featureGroupToData(fg) {
    console.log({fg})
    const features = fg.current.leafletElement.toGeoJSON().features;
    console.log({features})
    return features.map(feature => {
        console.log({feature})
        switch (feature.geometry.type) {
            case "Point": return geometryPointToData(feature.geometry);
            case "Polygon": return geometryPolygonToData(feature.geometry);
            default: return null
        }
    })
}

function BoxMarker(box) {
    return <Marker position={geographicElementToPoint(box)} />
}

function BoxRectangle(box) {
    return <Rectangle bounds={geographicElementToBounds(box)} />
}

function boxToElement(box) {
    if (geographicElementIsPoint(box)) {
        return <BoxMarker box={box} />
    } else {
        return <BoxRectangle box={box} />
    }
}

const BaseLayer = () => (
    <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution="&copy; <a href=&quot;http://osm.org/copyright&quot;>OpenStreetMap</a> contributors"
    />
)

export const BoxMap = ({ boxes, disabled, onChange, tickId }) => {

    const featureGroupRef = React.useRef(null);

    const defaultCenter = [-28, 134];

    const bounds = null;//boxesToExtents(boxes);

    const handleChange = () => {
        onChange(featureGroupToData(featureGroupRef))
    };

    const initialElements = boxes.map(boxToElement);

    return (
        <Map
            id="map"
            style={{
                "height": 500,
                "width": "map-width"
            }}
            useFlyTo={true}
            center={defaultCenter}
            zoom={4}
            keyboard={false}
            closePopupOnClick={false}
            bounds={bounds}
        >
            <BaseLayer />
            <FeatureGroup
                ref={featureGroupRef}
                key={"featureGroup" + tickId}
            >
                <EditControl
                    position="topright"
                    draw={{
                        "polyline": false,
                        "polygon": false,
                        "rectangle": {},
                        "circle": false,
                        "marker": {},
                        "circlemarker": false
                    }}
                    edit={{
                        edit: {},
                        remove: {},
                        poly: {}
                    }}
                    onEdited={handleChange}
                    onDeleted={handleChange}
                    onCreated={handleChange}
                />
                {initialElements}
            </FeatureGroup>
        </Map>
    );
};

BoxMap.propTypes = {
    boxes: PropTypes.array
};

BoxMap.defaultProps = {
    boxes: [],
    tickId: 1
};
