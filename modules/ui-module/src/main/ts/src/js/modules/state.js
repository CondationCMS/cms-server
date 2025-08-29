/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
class State {
    constructor(initialState = {}) {
        this.state = initialState;
        this.observers = [];

        return new Proxy(this, {
            get: (target, prop) => {
                if (prop in target.state) {
                    return target.state[prop];
                }

                return target[prop];
            },
            set: (target, prop, value) => {
                if (prop in target.state) {
                    if (target.state[prop] !== value) {
                        target.state[prop] = value;

                        this.observers.forEach(({ observer, dependencies }) => {
                            if (dependencies.has(prop)) {
                                observer(this.state);
                            }
                        });
                    }
                } else {
                    target[prop] = value;
                }
            },
        });
    }

    observe(observer) {
        const dependencies = new Set();

        const proxy = new Proxy(this.state, {
            get: (target, prop) => {
                dependencies.add(prop);
                return target[prop];
            },
        });

        observer(proxy);
        this.observers.push({ observer, dependencies });
    }

    unobserve(observer) {
        const index = this.observers.findIndex(entry => entry.observer === observer);
        if (index !== -1) {
            this.observers.splice(index, 1);
        }
    }
}
