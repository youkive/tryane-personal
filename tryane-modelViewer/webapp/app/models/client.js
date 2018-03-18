import EmberObject from '@ember/object';

export default EmberObject.extend({
    id: null,
    name: null,
    external_id: null,
    subscription_sips_id: null,
    status: null,
    payment_status: null,
    enabled: null,
    dedicated_url: null,
    modules: null,
});