import { LanguageMapping } from "app/core/external/rest/typed/language-mapping"

export class LocalizedText extends Map<string, string> {
    static from(mappings: Array<LanguageMapping>): LocalizedText {
        return mappings.reduce((acc, lm) => {
            acc.set(lm.languageCode, lm.content)
            return acc
        }, new LocalizedText())
    }
}