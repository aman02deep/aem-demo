use(function() {
    var i;
    for (i in this) {
        if (this[i] != null) {
            request.setAttribute(i, this[i]);
        }
    }
});
