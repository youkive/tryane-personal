import RSVP from "rsvp";
import $ from "jquery";

import Route from '@ember/routing/route';
import { A } from "@ember/array";

import Client from '../models/client';

export default Route.extend({
    model: function () {
        return new RSVP.Promise(function (resolve) {
            $.get('/api/v1/clients').done(function (data) { resolve(data) });
        }).then(function (data) {
            return A(data).map(function (item) {
                return Client.create(item);
            });
        });
    },
});
