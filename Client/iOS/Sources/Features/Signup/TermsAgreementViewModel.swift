import Combine
import Foundation

@MainActor
final class TermsAgreementViewModel: ObservableObject {
    @Published private(set) var agreements: [TermsAgreement] = [
        TermsAgreement(id: .service, title: "서비스 이용약관 동의", isRequired: true, isAccepted: false),
        TermsAgreement(id: .privacy, title: "개인정보처리방침 동의", isRequired: true, isAccepted: false)
    ]

    var isAllAccepted: Bool {
        agreements.allSatisfy(\.isAccepted)
    }

    var canContinue: Bool {
        agreements.filter(\.isRequired).allSatisfy(\.isAccepted)
    }

    var acceptedRequiredAgreements: [TermsAgreement] {
        agreements.filter { $0.isRequired && $0.isAccepted }
    }

    func toggleAll() {
        let nextValue = !isAllAccepted
        agreements = agreements.map {
            TermsAgreement(id: $0.id, title: $0.title, isRequired: $0.isRequired, isAccepted: nextValue)
        }
    }

    func toggle(_ id: TermsAgreementID) {
        agreements = agreements.map {
            guard $0.id == id else { return $0 }
            return TermsAgreement(id: $0.id, title: $0.title, isRequired: $0.isRequired, isAccepted: !$0.isAccepted)
        }
    }
}
