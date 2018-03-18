import Route from '@ember/routing/route'
import { A } from "@ember/array";

import RSVP from "rsvp";
import $ from "jquery";

import User from '../models/user';

export default Route.extend({

    model: function () {
        return new RSVP.Promise(function (resolve) {
            $.get('/users', function (data) {
                resolve(data);
            }).done(function (data) {
                return A(data).map(function (item) {
                    return User.create(item);
                });
            });
        });
    },
});