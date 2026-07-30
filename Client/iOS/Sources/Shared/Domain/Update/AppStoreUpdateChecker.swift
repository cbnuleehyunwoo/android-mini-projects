import Foundation

struct AppUpdate: Equatable {
    let version: String
    let storeURL: URL
}

protocol AppUpdateChecking {
    func availableUpdate() async throws -> AppUpdate?
}

struct AppStoreUpdateChecker: AppUpdateChecking {
    private let bundleIdentifier: String?
    private let currentVersion: String?
    private let countryCode: String
    private let session: URLSession

    init(
        bundle: Bundle = .main,
        countryCode: String = "kr",
        session: URLSession = .shared
    ) {
        bundleIdentifier = bundle.bundleIdentifier
        currentVersion = bundle.object(
            forInfoDictionaryKey: "CFBundleShortVersionString"
        ) as? String
        self.countryCode = countryCode
        self.session = session
    }

    init(
        bundleIdentifier: String,
        currentVersion: String,
        countryCode: String = "kr",
        session: URLSession = .shared
    ) {
        self.bundleIdentifier = bundleIdentifier
        self.currentVersion = currentVersion
        self.countryCode = countryCode
        self.session = session
    }

    func availableUpdate() async throws -> AppUpdate? {
        guard let bundleIdentifier,
              let currentVersion,
              let requestURL = lookupURL(bundleIdentifier: bundleIdentifier)
        else {
            return nil
        }

        let (data, response) = try await session.data(from: requestURL)
        guard let httpResponse = response as? HTTPURLResponse,
              (200..<300).contains(httpResponse.statusCode)
        else {
            throw URLError(.badServerResponse)
        }

        let lookupResponse = try JSONDecoder().decode(LookupResponse.self, from: data)
        guard let app = lookupResponse.results.first(where: { $0.bundleId == bundleIdentifier }),
              isNewerVersion(app.version, than: currentVersion),
              let storeURL = URL(string: app.trackViewUrl)
        else {
            return nil
        }

        return AppUpdate(version: app.version, storeURL: storeURL)
    }

    private func lookupURL(bundleIdentifier: String) -> URL? {
        var components = URLComponents(string: "https://itunes.apple.com/lookup")
        components?.queryItems = [
            URLQueryItem(name: "bundleId", value: bundleIdentifier),
            URLQueryItem(name: "country", value: countryCode),
        ]
        return components?.url
    }

    private func isNewerVersion(_ storeVersion: String, than currentVersion: String) -> Bool {
        currentVersion.compare(storeVersion, options: .numeric) == .orderedAscending
    }
}

private struct LookupResponse: Decodable {
    let results: [LookupApp]
}

private struct LookupApp: Decodable {
    let bundleId: String
    let version: String
    let trackViewUrl: String
}
