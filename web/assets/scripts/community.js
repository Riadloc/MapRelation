importScripts("jLouvain.js");
onmessage = function (e) {
    console.log('Message received from main script');
    let vertices = [];
    let Arcs = [];
    console.log(e.data);
    const reader = new FileReader();
    reader.readAsText(e.data);
    reader.onload = function () {
        let result = this.result.replace(/"/g, '').split(/\r\n/);
        const index = result.findIndex(function (item) {
            return item.includes('Arcs');
        });
        vertices = result.slice(1, index);
        Arcs = result.slice(index+1, -1);
        const node_data = vertices.map((item) => item.split(' ')[0]);
        const edge_data = Arcs.map((item) => {
            const arr= item.split(' ');
            return { source: arr[0], target: arr[1], weight: arr[2] }
        }).filter((item) => item.source != item.target);
        console.log('Posting message back to main script');
        console.log(node_data);
        console.log(edge_data);
        const community = jLouvain().nodes(node_data).edges(edge_data);
        const res = community();
        postMessage(res);
    };
};
