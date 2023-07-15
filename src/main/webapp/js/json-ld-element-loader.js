// If this is a Synapse place, get the ID and try to add the result to an application/ld+json type script element
const hash = window.location.hash;
if (hash.startsWith('#!Synapse:')) {
	// get the synapse ID
	const matchArray = /syn\d+/gi.exec(hash);
	if (matchArray.length > 0) {
		const synID = matchArray[0];
		fetch(`/Portal/jsonldcontent?entityId=${synID}`)
		.then(response => response.json())
		.then(data => {
			if (data) {
				const script = document.createElement('script');
				script.type = 'application/ld+json';
				script.innerHTML = JSON.stringify(data);
				document.head.appendChild(script);
			}
		})
		.catch(error => {
		  console.error('Error fetching JSON-LD data:', error);
		});
	}
}
