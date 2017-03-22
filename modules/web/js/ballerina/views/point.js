/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import _ from 'lodash';
import EventChannel from 'event_channel';

class Point extends EventChannel {
    constructor(x, y) {
        super();
        this._x = x || 0;
        this._y = y || 0;
    }

    /**
     * Gets or sets X coordinate of the Point.
     * @returns {number|void} X coordinate of the Point or void.
     */
    x(newX) {
        if (newX === undefined) {
            return this._x;
        }
        var dx = newX - this._x ;
        this._x = newX;
        this.trigger('moved', {dx: dx, dy: 0});
    }

    /**
     * Gets or sets Y coordinate of the Point.
     * @returns {number|void} Y coordinate of the Point or void.
     */
    y(newY) {
        if (newY === undefined) {
            return this._y;
        }
        var dy = newY - this._y;
        this._y = newY;
        this.trigger('moved', {dx: 0, dy: dy});
    }

    /**
     * Clone
     */
    clone() {
        return new Point(this._x, this._y);
    }

    /**
     * Move point by dx and dy
     * @param dx
     * @param dy
     */
    move(dx, dy) {
        this.x(this.x() + dx);
        this.y(this.y() + dy);
        return this;
    }

    /**
     * Returns absolute distance in X axis from a given Point to this point.
     * @param {Point} refPoint Referring point.
     * @returns {number} Absolute distance in X axis.
     */
    absDistInXFrom(refPoint) {
        return Math.abs(this.distInXFrom(refPoint))
    }

    /**
     * Returns absolute distance in Y axis from a given Point to this point.
     * @param {Point} refPoint Referring point.
     * @returns {number} Absolute distance in Y axis.
     */
    absDistInYFrom(refPoint) {
        return Math.abs(this.distInYFrom(refPoint))
    }

    /**
     * Returns distance in X axis from a given Point to this point.
     * @param {Point} refPoint Referring point.
     * @returns {number} Distance in X axis.
     */
    distInXFrom(refPoint) {
        return this.x() - refPoint.x();
    }

    /**
     * Returns distance in Y axis from a given Point to this point.
     * @param {Point} refPoint Referring point.
     * @returns {number} Distance in Y axis.
     */
    distInYFrom(refPoint) {
        return this.y() - refPoint.y();
    }

    /**
     * Returns the distance from a given Point to this point.
     * @param {Point} refPoint Referring point
     * @returns {number} Distance
     */
    distFrom(refPoint) {
        return Math.sqrt(Math.pow(this.absDistInXFrom(refPoint), 2)
            + Math.pow(this.absDistInYFrom(refPoint), 2));
    }
}

export default Point;
