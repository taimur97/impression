package com.afollestad.impression;

import android.app.Application;
import android.content.Context;

import com.afollestad.impression.accounts.base.Account;
import com.afollestad.impression.utils.PrefUtils;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.functions.Func1;

public class App extends Application {

    private static Account currentAccount;

    public static Single<Account> getCurrentAccount(Context context) {
        final int activeId = PrefUtils.getActiveAccountId(context);
        return Observable.create(new Observable.OnSubscribe<Account[]>() {
            @Override
            public void call(Subscriber<? super Account[]> subscriber) {
                if (currentAccount != null && currentAccount.id() == activeId) {
                    subscriber.onNext(new Account[]{currentAccount});
                }
                subscriber.onCompleted();
            }
        }).switchIfEmpty(Account.getAll(context).toObservable())
                .map(new Func1<Account[], Account>() {
                    @Override
                    public Account call(Account[] accounts) {
                        for (Account a : accounts) {
                            if (a.id() == activeId) {
                                currentAccount = a;
                                break;
                            }
                        }
                        return currentAccount;
                    }
                }).toSingle();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        //LeakCanary.install(this);
    }
}