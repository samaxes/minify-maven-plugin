/*
 * The TOC control dynamically builds a table of contents from the headings in
 * a document and prepends legal-style section numbers to each of the headings.
 */
samaxesJS.toc = function() {
    var window = this, document = this.document;

    /*
     * Create TOC element links.
     */
    function createLink(nodeId, innerHTML) {
        var a = document.createElement('a');
        if (nodeId !== '') {
            a.setAttribute('href', '#' + nodeId);
        }
        a.innerHTML = innerHTML;
        return a;
    }

    /*
     * Logging and debugging.
     */
    function debug(object) {
        if (window.console && window.console.log) {
            window.console.log(object);
        }
    }

    return function(toc) {
        toc = toc.appendChild(document.createElement('ul'));
        var i2 = 0, i3 = 0, i4 = 0;
        var nodes = document.body.childNodes;

        for (var i = 0; i < nodes.length; ++i) {
            var node = nodes[i];
            var tagName = node.nodeName.toLowerCase();
            var section;
            try {
                if (tagName === 'h4') {
                    ++i4;
                    if (i4 === 1) {
                        toc.lastChild.lastChild.lastChild.appendChild(document.createElement('ul'));
                    }
                    section = i2 + '.' + i3 + '.' + i4;
                    node.insertBefore(document.createTextNode(section + '. '), node.firstChild);
                    toc.lastChild.lastChild.lastChild.lastChild.appendChild(document.createElement('li')).appendChild(
                        createLink(node.id, node.innerHTML));
                } else if (tagName === 'h3') {
                    ++i3; i4 = 0;
                    if (i3 === 1) {
                        toc.lastChild.appendChild(document.createElement('ul'));
                    }
                    section = i2 + '.' + i3;
                    node.insertBefore(document.createTextNode(section + '. '), node.firstChild);
                    toc.lastChild.lastChild.appendChild(document.createElement('li')).appendChild(
                        createLink(node.id, node.innerHTML));
                } else if (tagName === 'h2') {
                    ++i2; i3 = 0; i4 = 0;
                    section = i2;
                    node.insertBefore(document.createTextNode(section + '. '), node.firstChild);
                    toc.appendChild(document.createElement('li')).appendChild(createLink(node.id, node.innerHTML));
                }
            } catch (error) {
                debug('Error message: ' + error.message);
            }
        }
    };
}();

// Export the symbols you want to keep when using a CompilationLevel of ADVANCED_OPTIMIZATIONS
window['samaxesJS'] = samaxesJS;
samaxesJS['toc'] = samaxesJS.toc;
