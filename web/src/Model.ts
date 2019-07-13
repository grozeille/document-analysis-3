export class SearchResult {
  documents: Array<SearchResultItem>;
  numFound: number;
  constructor() {
    this.documents = new Array<SearchResultItem>();
    this.numFound = 0;
  }
}

export class SearchResultItem {
  id: string;
  url: string;
  urlTxt: string;
  name: string;
  body: string;
  tags: Array<string>;

  constructor() {
    this.id = "";
    this.url = "";
    this.urlTxt = "";
    this.name = "";
    this.body = "";
    this.tags = new Array<string>();
  }
}