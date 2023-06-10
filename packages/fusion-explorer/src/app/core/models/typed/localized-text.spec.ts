import { LanguageMapping } from "app/core/external/rest/typed/language-mapping";
import { LocalizedText } from "./localized-text";

const MAPPINGS: Array<LanguageMapping> = [
    {
        languageId: '2102d805-71c9-45ba-b758-dab3d95928eb',
        textId: '5ef26d66-efcb-4f9c-a329-dd38e2485c48',
        content: 'a test text',
        languageCode: 'en-US'
    },
    {
        languageId: 'e08c2cf5-4889-43fd-8c03-1ca714525d87',
        textId: '5ef26d66-efcb-4f9c-a329-dd38e2485c48',
        content: 'un texte de test',
        languageCode: 'fr-FR'
    }
];

const EXPECTED_LOCALIZED_TEXT: LocalizedText = new LocalizedText([
    ['en-US', 'a test text'],
    ['fr-FR', 'un texte de test']
]);

describe('localized-test', () => {

    it('should create', () => {
        let lt = new LocalizedText();
        expect(lt instanceof LocalizedText).toBeTruthy();
    });

    it('should instantiate from language-mapping', () => {
        expect(LocalizedText.from(MAPPINGS)).toEqual(EXPECTED_LOCALIZED_TEXT);
    });
});